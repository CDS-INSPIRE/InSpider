define ([
	'dojo/_base/declare',
	'dojo/_base/array',
	'dojo/_base/lang',
	'dojo/Stateful',
	'dojo/Deferred',
	'dojo/request',
	'dojo/request/xhr',
	'dojo/json',
	'dojo/promise/all',
	'dojo/Evented',
	'dojo/on',
	'dojox/html/entities',
	'dojo/when',
	'dojo/query',
	'dojo/dom-geometry',
	'dojo/dom-construct',
	'put-selector/put',
	
	'./Operation',
	'./Block',
	'./AttributePanel',
	'./Row',
	'./ConditionRow',
	'./ConditionBlock',
	'./EmptyInput',
	'./PreviewWindow',
	
	'./form/EnumeratedValue'
], function (
		declare, 
		array, 
		lang, 
		Stateful, 
		Deferred, 
		request,
		xhr,
		json,
		all,
		Evented,
		on,
		htmlEntities,
		when,
		query,
		domGeometry,
		domConstruct,
		put,
		Operation, 
		Block, 
		AttributePanel, 
		Row, 
		ConditionRow, 
		ConditionBlock, 
		EmptyInput,
		PreviewWindow,
		EnumeratedValue) {

	var PING_TIMEOUT = 10 * 1000;
				
	function convertType (/*String*/type) {
		if (!type) {
			return {
				name: 'none',
				description: 'geen'
			};
		}
		
		switch (type) {
		case 'class java.lang.Object':
			return { 
				name: 'any',
				description: 'any'
			};
		case 'class [Ljava.lang.String;':
			return {
				name: 'string',
				description: 'lijst(tekst)'
			};
		case 'class java.lang.String':
			return { 
				name: 'string',
				description: 'tekst'
			};
		case 'class java.sql.Date':
			return {
				name: 'date',
				description: 'datum'
			};
		case 'class java.sql.Timestamp':
			return {
				name: 'datetime',
				description: 'datum/tijd'
			};
		case 'class java.sql.Time':
			return {
				name: 'time',
				description: 'tijd'
			};
		case 'class java.lang.Number':
		case 'class java.math.BigDecimal':
			return {
				name: 'decimal',
				description: 'getal'
			};
		case 'interface org.deegree.geometry.Geometry':
			return {
				name: 'geometry',
				description: 'geometrie'
			};
		case 'class nl.ipo.cds.attributemapping.NullReference':
			return {
				name: 'nullreference',
				description: 'lege waarde (NULL)'
			};
		case 'class org.deegree.commons.tom.ows.CodeType':
			return {
				name: 'code',
				description: 'code'
			};
		case 'class java.lang.Double':
		case 'double':
			return {
				name: 'double',
				description: 'double'
			};
		case 'class java.lang.Boolean':
		case 'boolean':
			return {
				name: 'boolean',
				description: 'boolean'
			};
		case 'class java.math.BigInteger':
		case 'class java.lang.Integer':
		case 'int':
			return {
				name: 'int',
				description: 'int'
			};
		case 'class java.lang.Float':
		case 'float':
			return {
				name: 'float',
				description: 'float'
			};
		case 'class java.lang.Character':
		case 'char':
			return {
				name: 'char',
				description: 'char'
			};
		case 'class java.lang.Short':
		case 'short':
			return {
				name: 'short',
				description: 'short'
			};
		case 'class java.lang.Long':
		case 'long':
			return {
				name: 'long',
				description: 'long'
			};
		}
		
		return {
			name: 'default',
			description: 'onbekend'
		};
	}
	
	var conditionFormDescriptions = {
		'attribute': 'Invoerattribuut',
		'operator': 'Operatie',
		'value': 'Waarde'
	};
	
	var conditionOperatorValues = {
		'in': 'Is gelijk aan',
		'not_in': 'Is ongelijk aan',
		'empty': 'Heeft geen waarde',
		'not_empty': 'Heeft een waarde'
	};
	
	return declare ([ Stateful, Evented ], {
		inputsUrl: null,
		operationsUrl: null,
		attributesUrl: null,
		mappingUrl: null,
		previewUrl: null,
		pingUrl: null,
		loginUrl: null,
		filterUrl: null,
		mappingPanel: null,
		
		attributes: null,
		inputFeatureType: null,
		
		hasPreview: false,
		
		startup: function () {
			console.log ('Startup: ', this.inputsUrl, this.operationsUrl, this.mappingPanel);
			
			// Resize the mapping panel:
			this.mappingPanel.resize ();
			
			// Refresh the mapping panel:
			this.refresh ();
			
			this.mappingPanel.on ('addcondition', lang.hitch (this, function (e) {
				this._addCondition (e.tree);
			}));
			
			this.mappingPanel.on ('savemapping', lang.hitch (this, function (e) {
				this._saveMapping (e.attributePanel);
			}));

			this.mappingPanel.on ('back', lang.hitch (this, function (e) {
				this.emit ('back');
			}));
			
			this.mappingPanel.on ('previewmapping', lang.hitch (this, function (e) {
				this.preview (this.get ('mappingPanel').get ('previewPanel'));
			}));
			
			this.mappingPanel.on ('selectpanel', lang.hitch (this, function (e) {
				console.log ('Selecting panel ', e.panel);
				if (e.panel == 'preview' && !this.hasPreview) {
					this.preview (this.mappingPanel.get ('previewPanel'));
				}
			}));
			
			this.mappingPanel.on ('saveconditions', lang.hitch (this, function (e) {
				this._saveConditions (e.conditionPanel, e.conditions);
			}));
			
			window.onbeforeunload = lang.hitch (this, this._onBeforeUnload);
			
			setTimeout (lang.hitch (this, this.ping), PING_TIMEOUT);
		},

		ping: function () {
			xhr.get (this.pingUrl, {
				handleAs: 'text',
				timeout: 5000
			}).response.then (lang.hitch (this, function (response) {
				setTimeout (lang.hitch (this, this.ping), PING_TIMEOUT);
				if (response.text.charAt (0) != '{') {
					// Session expired:
					this.showStatus (put (
						'div p $ a[href=#] $ << p $ <',
						'Opnieuw inloggen is noodzakelijk om de verbinding met de server te herstellen.',
						'Klik hier om opnieuw in te loggen.',
						'Wijzigingen in de attribuutmapping kunnen mogelijk niet worden opgeslagen.'
					));
					return;
				}
				this.showStatus (null);
			}), lang.hitch (this, function (response) {
				// Disconnected:
				setTimeout (lang.hitch (this, this.ping), PING_TIMEOUT);
				this.showStatus (put (
					'div p $ < p $ <', 
					'De verbinding met de server is verbroken. Controleer de netwerkverbinding.', 
					'Wijzigingen in de attribuutmapping kunnen mogelijk niet worden opgeslagen.'
				));
			}));
		},
		
		showStatus: function (/*String|DOMNode*/message) {
			if (this.statusMessage) {
				domConstruct.destroy (this.statusMessage);
			}
			if (this.statusMessageHandler) {
				this.statusMessageHandler.remove ();
			}
			if (message) {
				this.statusMessage = put (query ('body')[0], 'div.cdsMappingMessage', message);
				var link = query ('a', this.statusMessage)[0];
				if (link) {
					this.statusMessageHandler = on (link, 'click', lang.hitch (this, function (e) {
						e.preventDefault ();
						window.open (this.loginUrl, 'loginWindow', 'resizable,scrollbars,status,width=600,height=300');
					}));
				}
			}
			return this.statusMessage;
		},
			
		refresh: function () {
			var self = this;
			
			this.mappingPanel.set ('loading', true);
			
			all ({
				inputs: this._loadJson (this.inputsUrl, 'inputs'),
				operations: this._loadJson (this.operationsUrl, 'operations'),
				attributes: this._loadJson (this.attributesUrl, 'attributes'),
				filter: this._loadJson (this.filterUrl, 'filter'),
				mapping: this._loadMapping ()
			}).then (function (/*Object*/results) {
				self._setInputs (results.inputs);
				self._setOperations (results.operations.operationTypes, results.operations.typeDictionary);
				self._setAttributes (results.attributes.attributes, results.mapping.mappings, results.operations.typeDictionary);
				self._setFilter (results.filter);
				
				console.log ('Operations: ', results.operations);
				console.log ('Inputs: ', results.inputs);
				console.log ('Attributes: ', results.attributes);
				console.log ('Mapping: ', results.mapping);
				console.log ('Filter: ', results.filter);
				
				self.mappingPanel.set ('loading', false);
			}, function (/*String*/error) {
				console.log ('Error starting attribute mapping!');
				self.mappingPanel.set ('loading', false);
				self.mappingPanel.set ('error', true);
				self.mappingPanel.set ('errorMessage', error);
			});
		},
		
		_loadMapping: function () {
			var deferred = new Deferred ();
			
			xhr.get (this.mappingUrl, {
				handleAs: 'json'
			}).then (lang.hitch (this, function (data) {
				deferred.resolve (data);
			}), lang.hitch (this, function () {
				deferred.reject ();
			}));
			
			return deferred;
		},
		
		save: function (/*String*/attributeName) {
			var deferred = new Deferred ();
			
			this._message (attributeName, 'Bezig met opslaan attribuutmapping ...', 'working');
			
			when (this.serialize (attributeName), lang.hitch (this, function (mapping) {
				if (mapping == null) {
					return;
				}
				
				xhr.post (this.mappingUrl + '/' + attributeName, {
					data: json.stringify (mapping),
					handleAs: 'json',
					headers: {
						'Content-Type': 'application/json'
					}
				}).then (lang.hitch (this, function (data) {
					if (data.error) {
						this._message (attributeName, 'De attribuutmapping kon niet worden opgeslagen: de mapping is ongeldig.', 'error');
						deferred.reject ();
					} else {
						var panel = this._getAttribute (attributeName).panel;
						
						panel.set ('modified', false);
						
						this._message (attributeName, 'De attribuutmapping is opgeslagen.', 'success');
						deferred.resolve ();
					}
				}), lang.hitch (this, function () {
					this._message (attributeName, 'Het opslaan van de attribuutmapping is mislukt.', 'error');
					deferred.reject ();
				}));
			}));
			
			return deferred;
		},
		
		preview: function (/*PreviewWindow*/previewPanel) {
			var deferred = new Deferred ();
			if (!this.attributes) {
				deferred.resolve ();
				return deferred;
			}

			// Collect promises for all attributes:
			var mappingPromises = { };
			array.forEach (this.attributes, function (attribute) {
				mappingPromises[attribute.name] = this.serialize (attribute.name);
			}, this);
			
			previewPanel.set ('loading', true);
			
			all (mappingPromises).then (lang.hitch (this, function (mappings) {
				// Serialize mappings:
				xhr.post (this.previewUrl, {
					data: json.stringify (mappings),
					handleAs: 'json',
					headers: {
						'Content-Type': 'application/json'
					}
				}).then (lang.hitch (this, function (data) {
					this.hasPreview = true;
					if (data.error) {
						console.log ('Fetching preview failed: invalid mapping.');
						deferred.reject ();
						previewPanel.set ('loading', false);
						previewPanel.setFeatures ([], [], {});
						previewPanel.set ('messages', [
							{
								logLevel: 'ERROR',
								attribute: null,
								message: 'Technische fout bij het laden van de features.'
							}
						]);
						return;
					}
					
					previewPanel.set ('loading', false);
					
					var attributes = { };
					array.forEach (this.attributes, function (attribute) {
						attributes[attribute.name] = attribute.info.label;
					});
					previewPanel.setFeatures (data.inputFeatures, data.outputFeatures, attributes);
					
					// Display messages:
					if (data.logItems) {
						previewPanel.set ('messages', data.logItems);
					}
				}), lang.hitch (this, function () {
					this.hasPreview = true;
					previewPanel.set ('loading', false);
					previewPanel.setFeatures ([], [], {});
					previewPanel.set ('messages', [
						{
							logLevel: 'ERROR',
							attribute: null,
							message: 'De features konden niet worden opgehaald, controleer de netwerkverbinding.'
						}
					]);
					deferred.reject ();
				}));
			}));
			
			return deferred;
		},
		
		serialize: function (/*String*/attributeName) {
			var attribute = this._getAttribute (attributeName),
				deferred = new Deferred ();
			
			if (!attribute) {
				deferred.resolve (null);
				return deferred;
			}
			
			when (attribute.panel.get ('blocks'), lang.hitch (this, function (blocks) {
				deferred.resolve ({
					featureTypeNamespace: this.inputFeatureType.name.namespace,
					featureTypeName: this.inputFeatureType.name.localPart,
					attributeName: attribute.name,
					operations: this._serializeBlocks (blocks)
				});
			}));
			
			return deferred;
		},
		
		_onBeforeUnload: function () {
			if (!this.attributes) {
				return;
			}
			
			var modified = false;
			
			for (var i = 0; i < this.attributes.length; ++ i) {
				var attribute = this.attributes[i];
				if (attribute.panel.get ('modified')) {
					modified = true;
					break;
				}
			}
			
			if (modified) {
				return "Niet alle attribuutmappings zijn opgeslagen.";
			}
		},
		
		_message: function (/*AttributePanel|String*/attribute, /*String*/message, /*String*/messageClass) {
			if (!attribute.isInstanceOf || !attribute.isInstanceOf (AttributePanel)) {
				attribute = this._getAttribute (attribute).panel;
			}
			if (!attribute) {
				return;
			}
			
			attribute.set ({
				message: message,
				messageClass: messageClass || 'info'
			});
		},
		
		_saveMapping: function (/*AttributePanel*/panel) {
			if (!this.attributes) {
				return;
			}
			
			var attributeName = null;
			
			// Locate the attribute:
			for (var i = 0; i < this.attributes.length; ++ i) {
				if (this.attributes[i].panel !== panel) {
					continue;
				}
			
				attributeName = this.attributes[i].name;
				break;
			}
			if (!attributeName) {
				return;
			}
			
			// Save the attribute:
			panel.set ('disabled', true);
			this.save (this.attributes[i].name).then (function () {
				panel.set ('disabled', false);
			}, function () {
				panel.set ('disabled', false);
			});
		},
		
		_saveConditions: function (/*ConditionPanel*/conditionPanel, /*Object*/conditions) {
			console.log ('_saveConditions: ', conditionPanel, conditions);
			
			conditionPanel.set ('disabled', true);
			
			xhr.post (this.filterUrl, {
				data: json.stringify (conditions),
				handleAs: 'json',
				headers: {
					'Content-Type': 'application/json'
				}
			}).then (lang.hitch (this, function () {
				conditionPanel.set ('disabled', false); 
				conditionPanel.clearModified ();
			}), lang.hitch (this, function () { 
				conditionPanel.set ('disabled', false);
				conditionPanel.clearModified ();
			}));
		},
		
		_deserializeBlocks: function (/*Object*/mapping, /*Function*/outputValidator) {
			console.log ('Deserializing blocks: ', mapping);
			
			// Turn mapping into a list of rows:
			var rows = [ ];
			function processOperations (/*Array*/operations, /*Number*/rowIndex, /*Number*/offset) {
				var totalSize = 0;
				array.forEach (operations, function (op) {
					var operationSize = 1;
					while (rowIndex > rows.length - 1) {
						rows.push ([ ]);
					}
					
					// Reserve room in the row:
					console.log ('Reserving room, ', offset);
					var row = rows[rowIndex],
						rowSize = 0;
					for (var i = 0; i < row.length; ++ i) {
						rowSize += row[i] ? row[i].size : 1;
					}
					while (rowSize < offset) {
						row.push (null);
						++ rowSize;
					}
					
					// Push this item 
					rows[rowIndex].push (op);
					
					if (op) {
						if (op.operationInputs) {
							operationSize = Math.max (operationSize, processOperations (op.operationInputs, rowIndex + 1, offset));
						}
						op.size = operationSize;
					}
					totalSize += operationSize;
					offset += operationSize;
				});
				return totalSize;
			}
			processOperations (mapping.operations, 0, 0);
			
			if (rows.length == 0) {
				return [{ 
					rowClass: ConditionRow,
					blocks: [ this._createConditionBlock (null, 1, true, outputValidator) ]
				}];
			}
			
			// Turn each row into a list of blocks:
			var factory = {
				'condition': lang.hitch (this, function (operation, isLast) {
					console.log ('Creating condition block ', operation, isLast)
					return this._createConditionBlock (operation.settings, operation.size, isLast, outputValidator);
				}),
				'operation': lang.hitch (this, function (operation, isLast) {
					return this._constructBlock (operation.name, operation.size, operation.settings);
				}),
				'placeholder': lang.hitch (this, function () {
					return new EmptyInput ();
				}),
				'input': lang.hitch (this, function (operation) {
					return this._constructInput (operation.name, operation.inputAttributeType);
				})
			};
			
			var resultRows = [ ];
			array.forEach (rows, function (row) {
				var blocks = [ ],
					hasConditions = false;
				
				array.forEach (row, function (operation, index) {
					if (operation && operation.type == 'condition') {
						hasConditions = true;
					}
					var operationType = operation ? operation.type : 'placeholder'; 
					blocks.push (factory[operationType] (operation, index == row.length - 1));
				}, this);
				
				resultRows.unshift ({
					rowClass: hasConditions ? ConditionRow : Row,
					blocks: blocks
				});
			}, this);
			
			console.log (rows);
			
			return resultRows;
		},
		
		_serializeBlocks: function (/*Array*/rows) {
			var lastRow = [ ];
			
			function serializeBlock (/*Block*/block, /*Number*/offset, /*Array*/lastRow) {
				var width = block.width,
					blockResult = {
						name: block.data.name,
						type: block.data.type
					};

				if (block.data.type == 'condition' || block.data.type == 'operation') {
					// Serialize settings object:
					blockResult.settings = { };
					if (block.data.settings) {
						var settings = block.data.settings;
						for (var i in settings) {
							if (!settings.hasOwnProperty (i) || i.charAt (0) == '_') {
								continue;
							}
							
							var value = settings[i];
							blockResult.settings[i] = value.get ? value.get ('value') : value;
						}
					}
					
					// Serialize inputs:
					var filteredInputs = array.filter (lastRow, function (b) { return b.offset >= offset && b.offset < offset + width; }),
						currentOffset = offset;
					blockResult.operationInputs = [ ];
					for (var i = 0; i < filteredInputs.length; ++ i) {
						var b = filteredInputs[i];
						
						while (currentOffset < b.offset) {
							blockResult.operationInputs.push (null);
							++ currentOffset;
						}
						
						blockResult.operationInputs.push (b.block);
						currentOffset += b.width;
					}
					
				} else if (block.data.type == 'input') {
					blockResult.inputAttributeType = block.data.returnType;
				}
				
				return {
					offset: offset,
					width: width,
					block: blockResult
				};
			}
			
			array.forEach (rows.slice (0, rows.length - 1), function (/*Array*/row) {
				var offset = 0,
					resultRow = [ ];
				
				array.forEach (row, function (/*Block*/block) {
					if (block.isInstanceOf (Block)) {
						resultRow.push (serializeBlock (block, offset, lastRow));
					}
					
					offset += block.width;
				}, this);
				
				lastRow = resultRow;
			}, this);
			
			return array.map (lastRow, function (b) { return b.block; });
		},
		
		_getAttribute: function (/*String*/attributeName) {
			if (!this.attributes) {
				return null;
			}
			
			for (var i = 0; i < this.attributes.length; ++ i) {
				if (this.attributes[i].name == attributeName) {
					return this.attributes[i];
				}
			}
			
			return null;
		},
		
		_createConditionBlock: function (/*Object?*/settings, /*Number?*/size, /*Boolean?*/isLast, /*Function?*/validator) {
			var attributes = { };
			
			if (size === undefined || size < 1) {
				size = 1;
			}
			
			if (this.inputFeatureType) {
				array.forEach (this.inputFeatureType.attributes, function (attribute) {
					attributes[attribute.name.localPart] = attribute.name.localPart;
				});
			}
			
			var dataSettings;
			if (!isLast) {
				dataSettings = new Stateful ({
					_descriptions: conditionFormDescriptions,
					attribute: new EnumeratedValue (settings && settings.attribute ? settings.attribute : null, attributes),
					operator: new EnumeratedValue (settings && settings.operator ? settings.operator : 'in', conditionOperatorValues),
					value: settings && settings.value ? settings.value : ''
				});
				
				// Add a handler that is triggered
				dataSettings._onModified = function (form) {
					var operator = dataSettings.get ('operator').get ('value'),
						hasValue = operator != 'empty' && operator != 'not_empty';
						
					form.setEnabled ('value', hasValue);
					if (!hasValue) {
						dataSettings.set ('value', '');
					}
				};
			} else {
				dataSettings = new Stateful ({ });
			}
			
			return new ConditionBlock ({
				fixed: true,
				noInputs: false,
				noTypes: true,
				canEdit: !isLast,
				canDelete: !isLast,
				lastCondition: !!isLast,
				solid: true,
				minInputs: 1,
				width: size,
				typeDescription: 'Conditie',
				labelPattern: function (block) {
					if (block.lastCondition) {
						return 'Geen conditie / anders';
					}
					
					var settings = block.data.settings,
						attribute = settings.get ('attribute'),
						operator = settings.get ('operator'),
						value = settings.get ('value');

					if (attribute.get ('value') == null) {
						return '(geen conditie)';
					} else if (operator.get ('value') == 'in' || operator.get ('value') == 'not_in') {
						return attribute.get ('label') + ' ' + operator.get ('label') + ' "' + htmlEntities.encode ('' + value) + '"'; 
					} else {
						return attribute.get ('label') + ' ' + operator.get ('label');
					}
				},
				data: {
					name: '[condition]',
					type: 'condition',
					settings: dataSettings
				},
				validate: validator
			});
		},
		
		_addCondition: function (/*Tree*/tree) {
			var conditionBlock = this._createConditionBlock ();			
			tree.addCondition (conditionBlock);
		},
		
		/**
		 * Creates a tab with an operation tree for each attribute.
		 */
		_setAttributes: function (/*Array*/attributes, /*Array*/mappingsList, /*Object*/typeDictionary) {
			var mappings = { };
			array.forEach (mappingsList, function (mapping) {
				mappings[mapping.attributeName] = mapping;
			});
			
			attributes.sort (function (a, b) {
				if (a.label < b.label) {
					return -1;
				} else if (a.label > b.label) {
					return 1;
				}
				return 0;
			});
			
			this.attributes = array.map (attributes, function (attribute) {
				return {
					name: attribute.name,
					panel: new AttributePanel ({
						title: attribute.label,
						blocks: this._createAttributeBlocks (attribute, mappings[attribute.name], typeDictionary),
						description: attribute.description
					}),
					info: attribute
				};
			}, this);
			
			this.mappingPanel.set ('attributes', array.map (this.attributes, function (a) { return a.panel; }));
		},
		
		_createAttributeBlocks: function (/*Object*/attribute, /*Object*/mapping, /*Object*/typeDictionary) {
			var type = convertType (attribute.type),
				outputBlock = new Block ({
					width: 1,
					solid: true,
					minInputs: 1,
					fixed: true,
					canDelete: false,
					canEdit: false,
					label: attribute.label,
					noOutput: true,
					isTarget: true,
					
					typeDescription: 'Type: ' + type.description,
					outputType: type.name,
					outputTypeDescription: type.description
	        	});
			
			var outputValidator = lang.hitch (this, function (block, inputs, validator) {
				this._validateOutput (block, inputs, outputBlock, attribute, typeDictionary, validator);
			});
			
			var mappedBlocks = this._deserializeBlocks (mapping, outputValidator),
				width = 0;
			if (mappedBlocks[mappedBlocks.length - 1]) {
				console.log (mappedBlocks);
				array.forEach (mappedBlocks[mappedBlocks.length - 1].blocks, function (b) { console.log ('Output block: ', b, b.get ('width')); width += Math.max (1, b.get ('width')); });
			}
			outputBlock.set ('width', Math.max (1, width));
			mappedBlocks.push ({
				rowClass: Row,
				blocks: [ outputBlock ]
			});
			
			console.log ('Output block width: ', width);
			
			return mappedBlocks;

			return [
		        {
		        	rowClass: Row,
		        	blocks: [
			        	new EmptyInput ({
							width: 1,
							solid: false,
							minInputs: 0
			        	})
		        	]
		        },
		        {
		        	rowClass: ConditionRow,
		        	blocks: [
			        	new ConditionBlock ({
							width: 1,
							solid: true,
							minInputs: 1,
							fixed: true,
							canEdit: false,
							canDelete: false,
							label: 'Geen conditie / anders',
							data: {
								name: '[condition]',
								type: 'condition',
								settings: new Stateful ({ })
							},
							
							validate: lang.hitch (this, function (block, inputs, validator) {
								this._validateOutput (block, inputs, outputBlock, attribute, typeDictionary, validator);
							})
			        	})
		        	]
		        },
		        {
		        	rowClass: Row,
		        	blocks: [ outputBlock ]
		        }
			];
		},
		
		_constructInput: function (/*String*/attributeName, /*String*/attributeType) {
			var attributeKey = attributeName + ': ' + attributeType,
				type = convertType (attributeType);
			
			if (this.inputFactories[attributeKey]) {
				return this.inputFactories[attributeKey] ();
			} else {
				return new Block ({
					typeDescription: 'Invoerattribuut',
					minInputs: 0,
					width: 1,
					label: attributeName,
					solid: true,
					fixed: false,
					noInputs: true,
					canEdit: false,
					
					outputType: type.name,
					outputTypeDescription: type.description,
					
					data: {
						name: attributeName,
						type: 'input',
						returnType: attributeType
					},
					
					validate: function (block, inputs, validator) {
						validator.reject (block, 'Het invoerattribuut bestaat niet meer in het bronschema');
					}
				});
			}
		},
		
		/**
		 * Takes an input feature type and updates the input attribute list in the
		 * mapping panel.
		 */
		_setInputs: function (/*Object*/inputFeatureType) {
			console.log ('_setInputs: ', inputFeatureType);
			this.inputFeatureType = inputFeatureType;

			this.mappingPanel.inputPane.set ('title', 'Invoerattributen: ' + inputFeatureType.name.localPart);
			
			this.inputFactories = { };
			
			var filteredAttributes = array.filter (inputFeatureType.attributes, function (attribute) {
				return !attribute.filterOnly;
			});
			
			var operations = array.map (filteredAttributes, function (attribute) {
				var type = convertType (attribute.type),
					attributeKey = attribute.name.localPart + ': ' + attribute.type;

				var blockFactory = this.inputFactories[attributeKey] = function () {
					return new Block ({
						typeDescription: 'Invoerattribuut',
						minInputs: 0,
						width: 1,
						label: attribute.name.localPart,
						solid: true,
						fixed: false,
						noInputs: true,
						canEdit: false,
						
						outputType: type.name,
						outputTypeDescription: type.description,
						
						data: {
							name: attribute.name.localPart,
							type: 'input',
							returnType: attribute.type
						}
					});
				};
				
				return new Operation ({
					title: attribute.name.localPart,
					type: type.name,
					typeDescription: type.description,
					blockFactory: blockFactory,
					isInput: true
				});
			}, this);
			
			this.mappingPanel.set ('featureType', inputFeatureType);
			this.mappingPanel.set ('inputs', operations);
		},
		
		_constructBlock: function (/*String*/operationName, /*Number*/size, /*Object?*/settings) {
			if (!this.blockFactories[operationName]) {
				return;
			}
			
			return this.blockFactories[operationName] (settings, size);
		},
		
		/**
		 * Takes a list of available operations and updates the available operations list
		 * in the mapping panel.
		 */
		_setOperations: function (/*Array*/operationTypes, /*Object*/typeDictionary) {
			var self = this;
			this.blockFactories = { };
			
			this.mappingPanel.set ('operations', array.map (operationTypes, function (operationType) {
				var hasInputs = operationType.inputs.length > 0,
					returnType = convertType (operationType.returnType),
					inputType = convertType (hasInputs ? operationType.inputs[0].type : null),
					operationName = operationType.name;
			
				var blockFactory = this.blockFactories[operationName] = function (/*Object?*/settings, /*Number?*/size) {
					if (size === undefined || size < 1) {
						size = 1;
					}
					return new Block ({
						typeDescription: hasInputs ? 'Transformatie' : operationType.label,
						minInputs: operationType.inputs.length,
						width: size,
						labelPattern: operationType.formatLabel,
						solid: true,
						fixed: false,
						noInputs: !hasInputs,
						canEdit: operationType.hasFields,
						
						outputType: returnType.name,
						outputTypeDescription: returnType.description,
						
						validate: function (block, inputs, validator) {
							self._validateOperationBlock (block, inputs, operationType, typeDictionary, validator);
						},
						
						data: {
							name: operationType.name,
							type: 'operation',
							returnType: operationType.returnType,
							fieldDescriptions: operationType.fieldDescriptions,
							settings: self._createBlockSettings (operationType.fieldDescriptions, settings),
							inputs: !operationType.inputs ? [ ] : array.map (operationType.inputs, function (input) {
								var type = convertType (input.type);
								return {
									name: input.name,
									description: input.description,
									type: input.type,
									typeName: type.name,
									typeDescription: type.description,
									variableInputCount: input.variableInputCount
								}
							})
						}
					});
				};
				
				return new Operation ({
					title: operationType.label,
					description: operationType.label,
					type: returnType.name,
					typeDescription: returnType.description,
					blockFactory: blockFactory,
					operationDescription: operationType.description,
					isInput: !operationType.inputs || operationType.inputs.length == 0
				});
			}, this));
		},
		
		_createBlockSettings: function (/*Array*/fieldDescriptions, /*Object?*/initialSettings) {
			var settings = new Stateful ();
			
			settings._descriptions = { };
			
			for (var i = 0; i < fieldDescriptions.length; ++ i) {
				var n = fieldDescriptions[i].name;
				settings[n] = initialSettings && initialSettings[n] ? initialSettings[n] : fieldDescriptions[i].defaultValue;
				settings._descriptions[fieldDescriptions[i].name] = fieldDescriptions[i].description;
			}
			
			return settings;
		},
		
		_validateOperationBlock: function (/*Block*/block, /*Block[]*/inputBlocks, /*Object*/operationType, /*Object*/typeDictionary, /*Object*/validator) {
			var inputs = operationType.inputs,
				hasVarargs = inputs.length > 0 ? inputs[inputs.length - 1].variableInputCount : false,
				minInputCount = hasVarargs ? inputs.length - 1 : inputs.length,
				length = hasVarargs ? inputBlocks.length : Math.min (minInputCount, inputBlocks.length);

			if (inputBlocks.length < minInputCount) {
				validator.reject (block, 'Onvoldoende invoerwaarden, verwacht: ' + minInputCount);
			}
			if (!hasVarargs && inputBlocks.length > minInputCount && !(inputs.length == 0 && inputBlocks.length == 1 && !inputBlocks[0])) {
				validator.reject (block, 'Te veel invoerwaarden, verwacht: ' + minInputCount);
			}
			
			
			for (var i = 0; i < length; ++ i) {
				var input = inputs[Math.min (i, inputs.length - 1)],
					inputBlock = inputBlocks[i];
				
				// Report missing inputs:
				if (!inputBlock) {
					validator.reject (block, 'Invoerwaarde ' + (i + 1) + ' ontbreekt.');
					continue;
				}
				
				// Report type mismatches:
				if (!this._areTypesAssignable (input.type, inputBlock.data.returnType, typeDictionary)) {
					validator.reject (inputBlock, 'Ongeldig type `' + convertType (inputBlock.data.returnType).description + '`, verwacht: `' + convertType (input.type).description + '`');
				}
			}
		},
		
		_validateOutput: function (/*Block*/block, /*Block[]*/inputs, /*Block*/outputBlock, /*Object*/attribute, /*Object*/typeDictionary, /*Object*/validator) {
			if (inputs.length == 0 || !inputs[0]) {
				validator.reject (block, 'Er is nog geen waarde gekoppeld aan het uitvoerattribuut.');
				return;
			}
			
			if (inputs.length > 1) {
				validator.reject (block, 'Uitvoerattribuut heeft teveel waarden.');
			}
			
			if (!this._areTypesAssignable (attribute.type, inputs[0].data.returnType, typeDictionary)) {
				validator.reject (block, 'Ongeldig type van de uitvoerwaarde `' + convertType (inputs[0].data.returnType).description + '`, verwacht: `' + convertType (attribute.type).description + '`');
			}
		},
		
		_areTypesAssignable: function (/*String*/targetType, /*String*/sourceType, /*Object*/typeDictionary) {
			// Anything can be cast to an object:
			if (targetType == 'class java.lang.Object') {
				return true;
			}
			
			if (targetType == sourceType) {
				return true;
			}
			
			return !!(typeDictionary[sourceType] && typeDictionary[sourceType][targetType]);
		},

		_loadJson: function (/*String*/url, /*String*/description) {
			var requestDeferred = null,
				deferred = new Deferred (function (reason) {
					if (requestDeferred) {
						requestDeferred.cancel ();
					}
				});

			requestDeferred = request (url, {
				handleAs: 'json'
			}).then (function (data) {
				if (data.error) {
					deferred.reject (data.description || data.message || data.cause || 'Fout bij het laden van "' + description + '""');
				}
				
				deferred.resolve (data);
			}, function (data) {
				deferred.reject ('Fout bij de communicatie met de server.');
			});
			
			return deferred;
		},
		
		_setFilter: function (/*Object*/expression) {
			console.log ('Setting filter expression: ', expression);
			this.mappingPanel.get ('conditionPanel').set ('value', expression);
		}
	});
});