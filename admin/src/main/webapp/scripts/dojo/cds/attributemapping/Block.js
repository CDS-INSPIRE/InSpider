define ([
	'dojo/_base/declare',
	'./_BlockMixin',
	'./_BlockInputTypeMixin',
	'dijit/_TemplatedMixin',
	'dijit/_CssStateMixin',
	'./DndSource',
	
	'dojo/query',
	'dojo/_base/array',
	'dijit/registry',
	'dojo/_base/lang',

	'dojo/dom',
	'dojo/dom-class',
	'dojo/dom-construct',
	'dojo/dom-style',
	'dojo/dom-prop',
	
	'dojo/dnd/Target',

	'dijit/Tooltip',
	
	'dojox/html/entities',
	
	'dojo/text!./templates/AttributePanelBlock.html'
], function (
		declare, 
		BlockMixin, 
		BlockInputTypeMixin,
		TemplatedMixin, 
		CssStateMixin, 
		DndSource, 
		query, 
		array, 
		registry, 
		lang, 
		dom, 
		domClass, 
		domConstruct, 
		domStyle,
		domProp,
		Target, 
		Tooltip,
		htmlEntities,
		blockTemplate) {
	
	return declare ([BlockMixin, TemplatedMixin, CssStateMixin, BlockInputTypeMixin], {
		baseClass: 'cdsAttributePanelBlock',
		
		solid: true,
		
		minInputs: 1,
		
		templateString: blockTemplate,
		
		contentContainerNode: null,
		contentNode: null,
		titleNode: null,
		centerNode: null,
		marginTopNode: null,
		marginBottomNode: null,
		marginLeftNode: null,
		marginRightNode: null,
		downArrowNode: null,
		addContainerNode: null,
		errorIconNode: null,
		errorTooltip: null,
		
		dndSource: null,
		dndTargets: null,

		hasErrors: false,
		errors: '',
		fixed: false,
		noInputs: false,
		noOutput: false,
		noTypes: false,
		canDelete: true,
		canEdit: true,
		isTarget: false,
		
		typeDescription: 'Transformatie',
		outputType: 'string',
		outputTypeDescription: 'Tekst',
		label: null,
		labelPattern: null,
		
		_setHasErrorsAttr: function (/*Boolean*/hasErrors) {
			domClass[hasErrors ? 'add' : 'remove'] (this.domNode, this.baseClass + 'Errors');
			this._set ('hasErrors', hasErrors);
		},
		
		_setErrorsAttr: function (/*String*/errors) {
			this.errorTooltip.set ('label', errors);
			this._set ('errors', errors);
		},
		
		_setFixedAttr: function (/*Boolean*/fixed) {
			array.forEach ([this.marginLeftNode, this.marginRightNode, this.marginBottomNode, this.downArrowNode], function (node) {
				domStyle.set (node, 'visibility', fixed ? 'hidden' : '');
			});
			domClass[fixed ? 'add' : 'remove'] (this.domNode, this.baseClass + 'Fixed');
		},
		
		_setIsTargetAttr: function (/*Boolean*/isTarget) {
			array.forEach ([this.marginTopNode], function (node) {
				domStyle.set (node, 'visibility', isTarget ? 'hidden' : '');
			});
			this._set ('isTarget', isTarget);
		},
		
		_setNoTypesAttr: function (/*Boolean*/noTypes) {
			array.forEach ([this.outputTypeNode], function (node) {
				domStyle.set (node, 'display', noTypes ? 'none' : '');
			});
			this._set ('noTypes', noTypes);
		},
		
		_setNoInputsAttr: function (/*Boolean*/noInputs) {
			domStyle.set (this.marginTopNode, 'visibility', noInputs ? 'hidden' : '');
			domClass[noInputs ? 'add' : 'remove'] (this.domNode, this.baseClass + 'NoInputs');
		},
		
		_setNoOutputAttr: function (/*Boolean*/noOutput) {
			domClass[noOutput ? 'add' : 'remove'] (this.domNode, this.baseClass + 'NoOutput');
			this._set ('noOutput', noOutput);
		},
		
		_setCanEditAttr: function (/*Boolean*/canEdit) {
			domStyle.set (this.editNode, 'display', canEdit ? '' : 'none');
			this._set ('canEdit', canEdit);
		},
		
		_setCanDeleteAttr: function (/*Boolean*/canDelete) {
			domStyle.set (this.deleteNode, 'display', canDelete ? '' : 'none');
			this._set ('canDelete', canDelete);
		},

		_setTitleAttr: function (title) {
			this._set ('title', title);
			this.set ('label', title);
		},
		
		postCreate: function () {
			this.inherited (arguments);
			
			var self = this;
			
			if (!this.fixed) {
				this.dndSource = new DndSource (this.contentContainerNode, {
					// Container:
					type: 'operation',
					creator: lang.hitch (this, this._creator),
					
					// Selector:
					singular: true,
					autoSync: false,
					
					// Source:
					isSource: true,
					withHandles: false,
					selfCopy: false,
					selfAccept: false,
					copyOnly: true,
					accept: [],
					generateText: false,
					
					creator: function () {
						var span = domConstruct.create ('span');
						
						domConstruct.place (document.createTextNode (self.get ('label')), span);
						
						return {
							node: span
						};
					}
				});
			}
			
			this.dndTargets = [
			    this.createTarget (this.marginTopNode, 'top'),
				this.createTarget (this.marginBottomNode, 'bottom'),
				this.createTarget (this.marginLeftNode, 'left'),
				this.createTarget (this.marginRightNode, 'right'),
				this.createTarget (this.downArrowNode, 'bottom')
			];
			
			this.errorTooltip = new Tooltip ({
				connectId: this.errorIconNode,
				label: 'Geen fouten'
			});
			
			this._updateTitle ();
		},
		
		uninitialize: function () {
			this.inherited (arguments);
		
			if (this.dndSource) {
				this.dndSource.destroy ();
				this.dndSource = null;
			}
		},
		
		_updateTitle: function () {
			if (!this.labelPattern) {
				return;
			}
			
			var title;
			
			if (lang.isFunction (this.labelPattern)) {
				title = this.labelPattern (this);
			} else {
				var settings = this.data.settings || { };
				
				title = '' + this.labelPattern;
				
				for (var i in settings) {
					if (!settings.hasOwnProperty (i) || i.charAt (0) == '_') {
						continue;
					}
					
					var pattern = '${' + i + '}',
						rawValue = settings.get (i),
						value = htmlEntities.encode ('' + (rawValue.get ? rawValue.get ('label') : rawValue));
					
					title = title.replace (pattern, value);
				}
			}
			
			if (title == '') {
				title = '(Geen waarde)';
			}
			
			if (title != this.label) {
				this.set ('label', title);
			}
			
			if (this.titleNode) {
				console.log ('Setting tite: ', title);
				domProp.set (this.titleNode, 'innerHTML', title);
			}
		},
		
		_onClickDelete: function (e) {
			this.tree.removeBlock (this).then (lang.hitch (this, function () {
				this.destroyRecursive ();
			}));
			
			e.preventDefault ();
		},
		
		_onClickEdit: function (e) {
			e.preventDefault ();
			
			if (this.tree) {
				this.tree._onEditBlock (this);
			}
		},
		
		_onClickAddCondition: function (e) {
			e.preventDefault ();
		},
		
		_creator: function () {
			var node = domConstruct.create ('span');
			
			domConstruct.place (document.createTextNode (this.title), node, 'last');
			
			return {
				node: node,
				data: this,
				type: 'operation'
			};
		},
		
		_dropBlock: function (/*Block*/block, /*String*/direction) {
			console.log ('Dropping block: ', block, direction);
			
			// Test whether the block can be dropped here. Blocks without inputs
			// can only be inserted if:
			// - They are inserted to the left or the right of an existing block (they will never
			//   have a parent).
			// - They are inserted above a block that previously had no inputs.
			if (block.noInputs && direction != 'left' && direction != 'right') {
				if (direction == 'bottom') {
					return;
				}
				
				var inputs = this.tree._getBlockInputs (this);
				for (var i = 0; i < inputs.length; ++ i) {
					if (inputs[i].solid) {
						return;
					}
				}
			}
			
			// Insert the block at its new location in the tree:
			this.tree.addBlock (block, this, direction);
		}
	});
});