define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/_base/array',
	'dojo/query',
	'dojo/dom-class',
	'dojo/dom-style',
	'dojo/on',

	'put-selector/put',

	'dijit/registry',
	'dijit/_WidgetBase',
	'dijit/form/Button',
	'dijit/form/ToggleButton',
	'dijit/form/Select',
	'dijit/form/ValidationTextBox',
	'dijit/form/CheckBox',
	'dijit/Tooltip'
], function (
		declare,
		lang,
		array,
		query,
		domClass,
		domStyle,
		on,
		put,
		registry,
		_WidgetBase,
		Button,
		ToggleButton,
		Select,
		ValidationTextBox,
		CheckBox,
		Tooltip) {

	// Hard-coded list of possible operators:
	var operators = {
		'equals': {
			label: 'is gelijk aan'
		},
		'not_equals': {
			label: 'is niet gelijk aan'
		},
		'less_than': {
			label: 'kleiner dan'
		},
		'less_than_equal': {
			label: 'kleiner dan of gelijk aan'
		},
		'greater_than': {
			label: 'groter dan'
		},
		'greater_than_equal': {
			label: 'groter dan of gelijk aan'
		},
		'like': {
			label: 'komt overeen met patroon'
		},
		'in': {
			label: 'in lijst'
		},
		'not_null': {
			label: 'is aanwezig'
		}
	};

	var ConditionWidget = declare ([_WidgetBase], {
		baseClass: 'cdsConditionPanelCondition',

		operatorNode: null,
		filterNode: null,
		typeNode: null,
		fieldNode: null,
		operationNode: null,
		valueNode: null,
		labelNode: null,
		caseSensitiveNode: null,
		errorIconNode: null,

		first: false,

		fieldSelect: null,
		operationSelect: null,
		valueInput: null,
		caseSensitiveCheckBox: null,
		errorTooltip: null,

		container: null,
		featureType: null,

		modified: false,
		valid: true,

		value: null,

		buildRendering: function () {
			this.inherited (arguments);

			domClass.add (this, this.baseClass);

			// Create the DOM for this widget:
			this.operatorNode = put (this.domNode, 'div.logical-operator $', 'en');
			this.filterNode = put (this.domNode, 'div.filter');
			this.typeNode = put (this.filterNode, 'span.type');
			this.fieldNode = put (this.filterNode, 'label span.field');
			this.operationNode = put (this.filterNode, 'label span.operation');
			this.valueNode = put (this.filterNode, 'span span.value');
			var labelNode = this.labelNode = put (this.filterNode, 'label');
			put (labelNode, '$', 'Hoofdlettergevoelig ');
			this.caseSensitiveNode = put (labelNode, 'label span.case-sensitive', 'Hoofdlettergevoelig');
			put (this.filterNode, 'a.delete[href=#] span $', 'Verwijderen');
			this.errorIconNode = put (this.filterNode, 'span.cdsAttributePanelBlockErrorIcon');

			// Build select options:
			var operatorSelectOptions = [ ];
			for (var i in operators) {
				operatorSelectOptions.push ({
					label: operators[i].label,
					value: i
				});
			}

			// Create form inputs:
			this.fieldSelect = new Select ({

			}, this.fieldNode);
			this.operationSelect = new Select ({
				options: operatorSelectOptions
			}, this.operationNode);
			this.valueInput = new ValidationTextBox ({
			}, this.valueNode);
			this.caseSensitiveCheckBox = new CheckBox ({
			}, this.caseSensitiveNode);

			// Create the error tooltip:
			this.errorTooltip = new Tooltip ({
				connectId: this.errorIconNode,
				label: 'Geen fouten'
			});

			// Register event handlers:
			this.own (on (query ('a', this.filterNode)[0], 'click', lang.hitch (this, function (e) {
				this.emit ('removecondition', {
					bubbles: true,
					cancelable: true
				});
				e.preventDefault ();
			})));
			this.own (on (this.fieldSelect, 'change', lang.hitch (this, function (e) {
				this.set ('modified', true);
				this.value = null;
				this._updateSelectedField ();
				this._validate ();
			})));
			this.own (on (this.operationSelect, 'change', lang.hitch (this, function (e) {
				this.set ('modified', true);
				this._updateSelectedField ();
				this._validate ();
			})));
			this.own (on (this.valueInput, 'change', lang.hitch (this, function (e) {
				this.set ('modified', true);
				this._validate ();
			})));
			this.own (on (this.caseSensitiveCheckBox, 'change', lang.hitch (this, function (e) {
				this.set ('modified', true);
				this._validate ();
			})));

			this._setFirstAttr (this.first);
			this._setFeatureTypeAttr (this.featureType);
			this._updateSelectedField ();
			if (this.value) {
				this._setValueAttr (this.value);
			}
		},

		destroyRendering: function () {
			this.inherited (arguments);
		},

		_setFirstAttr: function (first) {
			this.first = first;

			if (this.operatorNode) {
				domStyle.set (this.operatorNode, 'display', first ? 'none' : '');
			}
		},

		_setFeatureTypeAttr: function (/*Object*/featureType) {
			this.featureType = featureType;

			if (!this.fieldSelect) {
				return;
			}

			var options = [ ];
			if (featureType && featureType.attributes) {
				// Create the options for the select:
				options = array.map (featureType.attributes, function (attribute) {
					return {
						label: attribute.label,
						value: attribute.name.localPart
					};
				});
			}

			options.unshift ({
				label: 'Kies een invoerveld ...',
				value: ''
			});

			var oldValue = this.fieldSelect.get ('value');
			this.fieldSelect.set ('options', options);
			this.fieldSelect.set ('value', oldValue ? oldValue : '');

			this._updateSelectedField ();
		},

		_getFeatureTypeAttr: function () {

		},

		_setValueAttr: function (/*Object*/value) {
			// Store the value for later if the widget hasn't been fully created yet:
			if (!this.fieldSelect || !this.operationSelect || !this.valueInput || !this.caseSensitiveCheckBox) {
				this.value = value;
				return;
			}

			if ('field' in value) {
				this.fieldSelect.set ('value', value.field);
			}
			if ('operation' in value) {
				this.operationSelect.set ('value', value.operation);
			}
			if ('value' in value) {
				this.valueInput.set ('value', value.value);
			}
			if ('caseSensitive' in value) {
				this.caseSensitiveCheckBox.set ('value', value.caseSensitive);
			}
		},

		_getValueAttr: function () {
			// Return the previously stored value if the widget hasn't been fully created yet:
			if (!this.fieldSelect || !this.operationSelect || !this.valueInput || !this.caseSensitiveCheckBox) {
				return this.value;
			}

			return {
				field: this.fieldSelect.get ('value'),
				operation: this.operationSelect.get ('value'),
				value: this.valueInput.get ('value'),
				caseSensitive: this.caseSensitiveCheckBox.get ('checked')
			};
		},

		_updateSelectedField: function () {
			var fieldName = this.fieldSelect.get ('value');
			console.log ('Changing field to: ', fieldName);
			if (!this.featureType || !fieldName || fieldName == '') {
				domStyle.set (this.labelNode, 'display', 'none');
				return;
			}

			var operation = this.operationSelect.get ('value');

			// Locate the attribute:
			var isString = false;
			array.forEach (this.featureType.attributes, function (attribute) {
				if (attribute.name.localPart == fieldName) {
					if (attribute.type == 'class java.lang.String') {
						isString = true;
					}
					this._updateValidation (attribute.type, operation);
				}
			}, this);

			domStyle.set (this.labelNode, 'display', isString && operation != 'is_null' ? '' : 'none');
		},

		_updateValidation: function (/*String?*/type, /*String?*/operation) {
			if (!operation) {
				operation = this.operationSelect.get ('value');
			}

			var valueInput = this.valueInput;
			this.set ('type', type);

			if (operation == 'not_null') {
				valueInput.set ('required', false);
				valueInput.set ('value', '');
				domStyle.set (valueInput.domNode, 'display', 'none');
			} else {
				domStyle.set (valueInput.domNode, 'display', '');

				switch (type) {
				default:
				case 'class java.lang.String':
					valueInput.set ('pattern', '.*');
					valueInput.set ('promptMessage', null);
					valueInput.set ('invalidMessage', null);
					valueInput.set ('required', false);
					break;
				case 'class java.sql.Date':
					valueInput.set ('promptMessage', 'Datum in het formaat DD-MM-JJJJ');
					valueInput.set ('invalidMessage', 'Datum in het formaat DD-MM-JJJJ');
					valueInput.set ('pattern', '[0-9]{2}\\-[0-9]{2}\\-[0-9]{4}');
					valueInput.set ('required', true);
					break;
				case 'class java.sql.Timestamp':
					valueInput.set ('promptMessage', 'Datum en tijd in het formaat DD-MM-JJJJ UU:MM');
					valueInput.set ('invalidMessage', 'Datum en tijd in het formaat DD-MM-JJJJ UU:MM');
					valueInput.set ('pattern', '[0-9]{2}\\-[0-9]{2}\\-[0-9]{4} [0-9]{2}\\:[0-9]{2}$');
					valueInput.set ('required', true);
					break;
				case 'class java.sql.Time':
					valueInput.set ('promptMessage', 'Tijd in het formaat UU:MM');
					valueInput.set ('invalidMessage', 'Tijd in het formaat UU:MM');
					valueInput.set ('pattern', '[0-9]{2}\\:[0-9]{2}');
					valueInput.set ('required', true);
					break;
				case 'interface org.deegree.geometry.Geometry':
				case 'float':
				case 'double':
					valueInput.set ('promptMessage', 'Vul een (gebroken) getal in');
					valueInput.set ('invalidMessage', 'Vul een (gebroken) getal in');
					valueInput.set ('pattern', '[0-9]+(\\.[0-9]+)?');
					valueInput.set ('required', true);
					break;
				case 'int':
					valueInput.set ('promptMessage', 'Vul een geheel getal in');
					valueInput.set ('invalidMessage', 'Vul een geheel getal in');
					valueInput.set ('pattern', '[0-9]+');
					valueInput.set ('required', true);
					break;
				case 'boolean':
					valueInput.set ('promptMessage', 'Waarde 0 of 1');
					valueInput.set ('invalidMessage', 'Waarde 0 of 1');
					valueInput.set ('pattern', '[01]');
					valueInput.set ('required', true);
					break;
				}

				// Force the valueinput to re-validate:
				var value = valueInput.get ('value');
				valueInput.set ('value', '');
				valueInput.set ('value', value);
			}
		},

		_validate: function () {
			var valueObject = this.get ('value'),
				field = valueObject.field,
				operation = valueObject.operation,
				value = valueObject.value,
				caseSensitive = valueObject.caseSensitive,
				type = this.type,
				valueValid = this.valueInput.isValid (),
				errors = [ ];

			console.log ('Validating: ', valueObject, valueValid, this.type);

			// Skip conditions without a field, they are considered empty:
			if (!field || field == '') {
				return;
			}

			// Like operator only works on strings:
			if (operation == 'like' && type != 'class java.lang.String') {
				errors.push ('De operatie "komt overeen met patroon" is alleen bruikbaar voor tekstvelden');
			}

			// The value must be valid:
			if (!valueValid) {
				errors.push ('De ingevulde waarde is ongeldig');
			}

			if (errors.length > 0) {
				var errorString = errors[0];
				for (var i = 1; i < errors.length; ++ i) {
					errorString += '\n' + errors[i];
				}
				this.errorTooltip.set ('label', errorString);
				domStyle.set (this.errorIconNode, 'display', 'block');
				this.set ('valid', false);
			} else {
				this.set ('valid', true);
				this.errorTooltip.set ('label', 'Geen fouten');
				domStyle.set (this.errorIconNode, 'display', 'none');
			}
		},

		_reportError: function (/*String*/errorMessage) {
			console.log ('Validation error: ', errorMessage);
			this.set ('valid', false);
		}
	});

	var ConditionContainerWidget = declare ([_WidgetBase], {
		baseClass: 'cdsConditionPanelContainer',

		outerContainerNode: null,
		containerNode: null,
		operationNode: null,
		footerNode: null,

		last: false,

		conditions: null,

		conditionPanel: null,
		featureType: null,

		modified: false,
		valid: true,

		value: null,

		buildRendering: function () {
			this.inherited (arguments);

			domClass.add (this, this.baseClass);

			// Create the DOM for this widget:
			this.outerContainerNode = put (this.domNode, 'div.outer-container');
			this.containerNode = put (this.outerContainerNode, 'div.container');
			this.footerNode = put (this.outerContainerNode, 'div.container-footer div.logical-operator $ a[href=#] span $ < < < div.footer-clear <', 'en', '+');
			this.operationNode = put (this.domNode, 'div.operator-container div.logical-operator $ <', 'of');

			// Register event handlers:
			this.own (on (query ('a', this.footerNode)[0], 'click', lang.hitch (this, function (e) {
				e.preventDefault ();

				this.createCondition ();
			})));
			this.own (on (this, 'removecondition', lang.hitch (this, function (e) {
				this.removeCondition (registry.byNode (e.target));

				e.preventDefault ();
				e.stopPropagation ();
			})));

			this._setLastAttr (this.last);

			// Add a single blank condition:
			this.createCondition ();

			if (this.value) {
				this._setValueAttr (this.value);
			}
		},

		destroyDescendants: function () {
			this.inherited (arguments);

			if (!this.conditions) {
				return;
			}

			array.forEach (this.conditions, function (condition) {
				condition.destroyRecursive ();
			});
		},

		destroyRendering: function () {
			this.inherited (arguments);
		},

		removeCondition: function (/*ConditionWidget*/condition) {
			if (!this.conditions || this.conditions.length == 0) {
				return;
			}

			console.log ('Removing condition: ', condition);

			for (var i = 0; i < this.conditions.length; ++ i) {
				console.log (this.conditions[i]);
				if (this.conditions[i] === condition) {
					console.log ('Removing');
					this.conditions.splice (i, 1);
					condition.destroyRecursive ();
					break;
				}
			}

			if (this.conditions.length == 0) {
				this.conditions = null;
				this.emit ('removecontainer', {
					bubbles: true,
					cancelable: true
				});
			} else {
				this._updateFirstCondition ();
			}

			this._updateModified (true);
			this._updateValid ();
		},

		_clear: function () {
			if (!this.conditions || this.conditions.length == 0) {
				return;
			}

			for (var i = 0; i < this.conditions.length; ++ i) {
				this.conditions[i].destroyRecursive ();
			}

			this.conditions = [];

			this._updateModified ();
			this._updateValid ();
		},

		createCondition: function () {
			this.addCondition (new ConditionWidget ({
				container: this,
				featureType: this.featureType
			}));
		},

		addCondition: function (/*ConditionWidget*/condition) {
			if (!this.conditions) {
				this.conditions = [ ];
			}
			this.conditions.push (condition);

			put (this.containerNode, condition.domNode);

			// Start the condition after adding it to the DOM:
			if (!condition._started) {
				condition.startup ();
			}

			this._updateFirstCondition ();

			// Watch for changes in the modified flag:
			this.own (condition.watch ('modified', lang.hitch (this, this._updateModified)));

			// Watch for changes in the valid flag:
			this.own (condition.watch ('valid', lang.hitch (this, this._updateValid)));

			// Update modified and valid flags:
			this._updateModified ();
			this._updateValid ();
		},

		_updateModified: function (/*Boolean?*/forceModified) {
			if (!this.conditions || this.conditions.length == 0 || forceModified) {
				this.set ('modified', true);
				return;
			}

			var modified = false;

			array.forEach (this.conditions, function (condition) {
				if (condition.get ('modified')) {
					modified = true;
				}
			}, this);

			this.set ('modified', modified);
		},

		_updateValid: function () {
			var valid = true;

			array.forEach (this.conditions, function (condition) {
				if (!condition.get ('valid')) {
					valid = false;
				}
			});

			this.set ('valid', valid);
		},

		_updateFirstCondition: function () {
			if (!this.conditions || this.conditions.length == 0) {
				return;
			}

			this.conditions[0].set ('first', true);
			for (var i = 1, length = this.conditions.length; i < length; ++ i) {
				this.conditions[i].set ('first', false);
			}
		},

		_setLastAttr: function (last) {
			this.last = last;

			if (this.operationNode) {
				domStyle.set (this.operationNode, 'display', last ? 'none' : '');
			}
		},

		_setFeatureTypeAttr: function (/*Object*/featureType) {
			this.featureType = featureType;

			if (this.conditions && this.conditions.length > 0) {
				array.forEach (this.conditions, function (condition) {
					condition.set ('featureType', featureType);
				});
			}
		},

		_getFeatureTypeAttr: function () {
			return this.featureType;
		},

		_getValueAttr: function () {
			if (!this.containerNode) {
				return this.value;
			}

			return {
				conditions: array.filter (array.map (this.conditions, function (condition) {
						return condition.get ('value');
					}),
					function (condition) {
						return condition.field && condition.field != '';
					})
			};
		},

		_setValueAttr: function (value) {
			if (!this.containerNode) {
				this.value = value;
				return;
			}

			this._clear ();

			if (!value.conditions) {
				return;
			}

			array.forEach (value.conditions, function (condition) {
				this.addCondition (new ConditionWidget ({
					container: this,
					featureType: this.featureType,
					value: condition
				}));
			}, this);
		}
	});

	return declare ([_WidgetBase], {
		baseClass: 'cdsConditionPanel',

		headerNode: null,
		helpNode: null,
		scrollPanelNode: null,
		containerNode: null,
		footerNode: null,

		saveButton: null,
		helpButton: null,

		containers: null,

		featureType: null,

		modified: false,
		valid: true,

		value: null,

		buildRendering: function () {
			this.inherited (arguments);

			domClass.add (this.domNode, this.baseClass);

			// Create DOM for this widget:
			this.headerNode = put (this.domNode, 'div.cdsAttributePanelHeader span.save < span.help <');
			this.helpNode = put (this.domNode, 'div.cdsAttributePanelInfo[style="display: none;"] p $ < p $ <', 'Een  gedeelte van een dataset uitsluiten van verdere verwerking kan in dit tabblad. Features in de WFS kunnen op basis van een selectie in een van de attributen “uitgefilterd” worden. Je kunt meerdere filters toevoegen: klik op de knop En of de knop Of om meerdere filters in te voeren.', 'De werking van de filter is te controleren op het tabblad Voorbeeld. De uitgesloten features krijgen de melding "Geen mapping mogelijk voor feature"');
			this.scrollPanelNode = put (this.domNode, 'div.scroll');
			put (this.scrollPanelNode, 'p $', 'Features verwerken als:');
			this.containerNode = put (this.scrollPanelNode, 'div');
			this.footerNode = put (this.scrollPanelNode, 'div.footer div.logical-operator $ a[href=#] span $ < <', 'of', '+');

			// Create buttons:
			this.saveButton = new Button ({
				label: 'Opslaan',
				iconClass: 'dijitIcon dijitIconSave'
			}, query ('.save', this.headerNode)[0]);
			this.helpButton = new ToggleButton ({
				label: 'Uitleg',
				iconClass: 'dijitIcon icon-help'
			}, query ('.help', this.headerNode)[0]);

			// Event handlers:
			this.own (on (query ('a', this.footerNode)[0], 'click', lang.hitch (this, function (e) {
				e.preventDefault ();

				this.createContainer ();
			})));
			this.own (on (this, 'removecontainer', lang.hitch (this, function (e) {
				this.removeContainer (registry.byNode (e.target));

				e.preventDefault ();
				e.stopPropagation ();
			})));
			this.own (on (this.helpButton, 'change', lang.hitch (this, function (e) {
				console.log ('update help text: ', this.helpButton.get ('checked'));
				domStyle.set (this.helpNode, 'display', this.helpButton.get ('checked') ? 'block' : 'none');
			})));
			this.own (on (this.saveButton, 'click', lang.hitch (this, function (e) {
				var value = this.get ('value');

				console.log ('Save: ', value);

				this.emit ('saveconditions', {
					bubbles: true,
					conditions: value,
					conditionPanel: this
				});
			})));

			// Add at least one container:
			this.createContainer ();

			this._updateModified ();
			this._updateValid ();
			this._updateSaveButton ();

			if (this.value) {
				this._setValueAttr (this.value);
			}
		},

		startup: function () {
			this.inherited (arguments);

			// Startup child widgets:
			this.saveButton.startup ();
			this.helpButton.startup ();
		},

		destroyDescendants: function () {
			this.inherited (arguments);

			if (!this.containers) {
				return;
			}

			array.forEach (this.containers, function (container) {
				container.destroyRecursive ();
			});

			this.containers = null;
		},

		destroyRendering: function () {
			this.inherited (arguments);

			this.saveButton.destroyRecursive ();
			this.helpButton.destroyRecursive ();

			this.saveButton = this.helpButton = null;
		},

		createContainer: function () {
			this.addContainer (new ConditionContainerWidget ({
				conditionPanel: this,
				featureType: this.featureType
			}));
		},

		addContainer: function (/*ConditionContainerWidget*/widget) {
			if (!this.containers) {
				this.containers = [ ];
			}

			this.containers.push (widget);

			put (this.containerNode, widget.domNode);

			// Start the widget after adding it to the DOM:
			if (!widget._started) {
				widget.startup ();
			}

			this._updateLastContainer ();

			// Watch for changes in the modified flag of the container:
			this.own (widget.watch ('modified', lang.hitch (this, function () {
				this._updateModified ();
				this._updateSaveButton ();
			})));

			// Watch for changes in the valid flag of the container:
			this.own (widget.watch ('valid', lang.hitch (this, function () {
				this._updateValid ();
				this._updateSaveButton ();
			})));

			// Update modified and valid flags:
			this._updateModified ();
			this._updateValid ();
			this._updateSaveButton ();
		},

		_updateModified: function (/*Boolean?*/forceModified) {
			var modified = !!forceModified;

			array.forEach (this.containers, function (container) {
				if (container.get ('modified')) {
					modified = true;
				}
			});

			console.log ('Updating modified flag: ', modified);
			this.set ('modified', modified);
		},

		_updateValid: function () {
			var valid = true;

			array.forEach (this.containers, function (container) {
				if (!container.get ('valid')) {
					valid = false;
				}
			});

			console.log ('Updating valid flag: ', valid);
			this.set ('valid', valid);
		},

		_updateSaveButton: function () {
			console.log ('_updateSaveButton: ', this.get ('modified'), this.get ('valid'), this.disabled);
			this.saveButton.set ('disabled', !this.get ('modified') || !this.get ('valid') || this.disabled);
		},

		removeContainer: function (/*ConditionContainerWidget*/container) {
			if (!this.containers || this.containers.length == 0) {
				return;
			}

			for (var i = 0; i < this.containers.length; ++ i) {
				if (this.containers[i] == container) {
					this.containers.splice (i, 1);
					container.destroyRecursive ();
				}
			}

			if (this.containers.length == 0) {
				this.containers = null;
				this.createContainer ();
				this.containers[0].set ('modified', true);
			}

			this._updateLastContainer ();

			this._updateModified (true);
			this._updateValid ();
			this._updateSaveButton ();
		},

		_clear: function () {
			if (!this.containers || this.containers.length == 0) {
				return;
			}

			for (var i = 0; i < this.containers.length; ++ i) {
				this.containers[i].destroyRecursive ();
			}
			this.containers = [ ];

			this._updateModified ();
			this._updateValid ();
			this._updateSaveButton ();
		},

		_updateLastContainer: function () {
			if (!this.containers || this.containers.length == 0) {
				return;
			}

			for (var i = 0, length = this.containers.length; i < length; ++ i) {
				this.containers[i].set ('last', i == length - 1);
			}
		},

		_setFeatureTypeAttr: function (/*Object*/featureType) {
			this.featureType = featureType;

			console.log ('_setFeatureTypeAttr: ', featureType);
			if (this.containers && this.containers.length > 0) {
				array.forEach (this.containers, function (container) {
					container.set ('featureType', featureType);
				});
			}
		},

		_getFeatureTypeAttr: function () {
			return this.featureType;
		},

		_getValueAttr: function () {
			// Return a previously stored value if the DOM hasn't been created yet.
			if (!this.containerNode) {
				return this.value;
			}

			return {
				conditionGroups: array.filter (array.map (this.containers, function (container) {
						return container.get ('value');
					}),
					function (container) {
						return container.conditions && container.conditions.length > 0;
					})
			};
		},

		_setValueAttr: function (value) {
			// Store the value if the DOM hasn't been created yet.
			if (!this.containerNode) {
				this.value = value;
				return;
			}

			this._clear ();

			if (!value || !value.conditionGroups) {
				return;
			}

			array.forEach (value.conditionGroups, function (group) {
				this.addContainer (new ConditionContainerWidget ({
					conditionPanel: this,
					featureType: this.featureType,
					value: group
				}));
			}, this);

			// Add at least one container:
			if (!this.containers || this.containers.length == 0) {
				this.createContainer ();
			}

			// Clear the modified flag:
			this.clearModified ();
		},

		_setDisabledAttr: function (disabled) {
			this._set ('disabled', disabled);

			this._updateSaveButton ();
		},

		clearModified: function () {
			array.forEach (this.containers, function (container) {
				array.forEach (container.conditions, function (condition) {
					condition.set ('modified', false);
				});
				container.set ('modified', false);
			});
			this.set ('modified', false);

			console.log ('_clearModified: ', this.modified, this.valid, this.disabled);
			this._updateSaveButton ();
		}
	});
});