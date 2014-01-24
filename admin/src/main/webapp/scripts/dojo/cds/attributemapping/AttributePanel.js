define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/query',
	'dojo/dom-class',
	'dojo/dom-style',
	'dojo/dom-prop',
	'dojox/html/entities',
	'dojo/fx',
	
	'./Tree',
	
	'dijit/layout/BorderContainer',
	'dijit/layout/ContentPane',
	'dijit/form/Button',
	'dijit/form/ToggleButton'
 ], function (declare, lang, query, domClass, domStyle, domProp, htmlEntities, fx, Tree, BorderContainer, ContentPane, Button, ToggleButton) {
	
 	return declare ([BorderContainer], {
 		// BorderContainer settings:
 		gutters: false,
 		liveSplitters: false,
 		
 		header: null,
 		infoBar: null,
 		treePane: null,
 		
 		tree: null,
 		saveButton: null,
 		overlayNode: null,
 		messageTextNode: null,
 		
 		blocks: null,

 		disabled: false,
 		message: '',
 		messageClass: 'info',
 		
 		description: null,
 		
 		postCreate: function () {
 			this.inherited (arguments);

 			// Build header:
 			this.header = new ContentPane ({
 				region: 'top',
 				content: '<span class="cdsAttributePanelSaveButton"></span><span class="cdsAttributePanelHelpButton"></span><div class="cdsAttributePanelMessageContainer"><span class="cdsAttributePanelMessageIcon"></span><span class="cdsAttributePanelMessageText"></div></div>'
 			});
 			domClass.add (this.header.containerNode, 'cdsAttributePanelHeader');
 			
 			this.infoBar = new ContentPane ({
 				region: 'top',
 				content: this.description,
 				style: 'display: none;'
 			});
 			domClass.add (this.infoBar.containerNode, 'cdsAttributePanelInfo');
 			
 			this.saveButton = new Button ({
 				style: 'float: right;',
 				label: 'Opslaan',
 				iconClass: 'dijitIconSave',
 				title: 'Sla de wijzigingen in de mapping voor dit attribuut op.'
 			}, query ('span.cdsAttributePanelSaveButton', this.header.containerNode)[0]);

 			this.helpButton = new ToggleButton ({
 				style: 'float: right;',
 				label: 'Uitleg',
 				iconClass: 'icon-help',
 				checked: false,
 				title: 'Toon uitleg bij dit attribuut.'
 			}, query ('span.cdsAttributePanelHelpButton', this.header.containerNode)[0]);
 			
 			this.own (this.saveButton.on ('click', lang.hitch (this, function () {
 				this.emit ('savemapping', {
 					bubbles: true,
 					attributePanel: this
 				});
 			})));
 			
 			this.own (this.helpButton.watch ('checked', lang.hitch (this, function (/*String*/attributeName, /*Boolean*/oldValue, /*Boolean*/newValue) {
 				this.infoBar.domNode.style.display = newValue ? '' : 'none';
 				this.helpButton.set ('title', (newValue ? 'Verberg' : 'Toon') + ' uitleg bij dit attribuut.');
 				this.resize ();
 			})));
 			
 			this.messageContainerNode = query ('.cdsAttributePanelMessageContainer', this.header.containerNode)[0];
 			this.messageTextNode = query ('.cdsAttributePanelMessageText', this.header.containerNode)[0];
 			
 			// Build operation tree:
 			this.treePane = new ContentPane ({
 				region: 'center',
 				content: '<div class="cdsAttributePanelTreeContainer"></div><div class="cdsAttributePanelOverlay"></div>'
 			});

 			this.addChild (this.header);
 			this.addChild (this.infoBar);
 			this.addChild (this.treePane);

 			this.overlayNode = query ('div.cdsAttributePanelOverlay', this.treePane.containerNode)[0];
 			domStyle.set (this.overlayNode, {
 				'opacity': 0.8,
 				'display': 'none'
 			});
 			
 			this.tree = new Tree ({ 
 			}, query ('div.cdsAttributePanelTreeContainer', this.treePane.containerNode)[0]);
 			
 			if (this.blocks) {
 				this.tree.setBlocks (this.blocks);
 				this.blocks = null;
 			}
 			
 			this.own (this.tree.watch ('valid', lang.hitch (this, function (attr, oldValue, newValue) {
 				this._updateValidModified (newValue, this.tree.get ('modified'));
 			})));
 			
 			this.own (this.tree.watch ('modified', lang.hitch (this, function (attr, oldValue, newValue) {
 				this._updateValidModified (this.tree.get ('valid'), newValue);
 			})));
 			
 			this._updateValidModified (this.tree.get ('valid'), this.tree.get ('modified'));
 			this._setMessageAttr (this.message);
 			this._setMessageClassAttr (this.messageClass);
 			if (this.modified !== undefined) {
 				this._setModifiedAttr (this.modified);
 			}
 		},
 		
 		_updateValidModified: function (/*Boolean*/valid, /*Boolean*/modified) {
 			var oldClass = this.get ('iconClass'),
 				newClass = valid  
					? (modified ? 'cdsAttributePanelIconOkModified' : 'cdsAttributePanelIconOk') 
	 				: (modified ? 'cdsAttributePanelIconErrorModified' : 'dijitIconError');
 			
 			this.set ('iconClass', newClass); 

			if (newClass != oldClass) {
				console.log ('Updating class: ', oldClass, newClass);

				this.emit ("resize", {
					bubbles: true
				});
			}
			
 			this._updateModified (modified);
 		},

 		
 		_updateModified: function (/*Boolean*/modified) {
 			this.saveButton.set ('disabled', !modified || this.disabled);
 			this._set ('modified', modified);
 			
 			if (this.controlButton) {
 				console.log ('_updateMofified ', this.controlButton);
 				domClass[modified ? 'add' : 'remove'] (this.controlButton.domNode, 'cdsAttributePanelTabModified');
 			}
 		},

 		_setModifiedAttr: function (/*Boolean*/modified) {
 			if (this.tree) {
 				this.tree.set ('modified', modified);
 			}
 			
 			this._set ('modified', modified);
 		},
 		
 		_getModifiedAttr: function () {
 			if (this.tree) {
 				return this.tree.get ('modified');
 			}
 		},
 		
 		_setMessageAttr: function (/*String*/message) {
 			this._set ('message', message);
 			
 			if (this.messageTextNode) {
 				domProp.set (this.messageTextNode, 'innerHTML', htmlEntities.encode (message));
 				domProp.set (this.messageTextNode, 'title', htmlEntities.encode (message));
 			}
 		},
 		
 		_setMessageClassAttr: function (/*String*/messageClass) {
 			var oldMessageClass = this.messageClass;
 			this._set ('messageClass', messageClass);
 			
 			if (this.messageContainerNode) {
	 			if (oldMessageClass) {
	 				domClass.remove (this.messageContainerNode, 'cdsAttributePanelMessageContainer-' + oldMessageClass);
	 			}
	 			domClass.add (this.messageContainerNode, 'cdsAttributePanelMessageContainer-' + messageClass);
 			}
 		},
 		
 		_setBlocksAttr: function (/*Array*/blocks) {
 			if (this.tree) {
 				this.tree.setBlocks (blocks);
 			} else {
 				this.blocks = blocks;
 			}
 		},
 		
 		_getBlocksAttr: function () {
 			if (this.tree) {
 				return this.tree.getBlocks ();
 			} else {
 				return this.blocks;
 			}
 		},
 	
 		_setDisabledAttr: function (/*Boolean*/disabled) {
 			this._set ('disabled', disabled);
 			
 			if (this.saveButton) {
 				this.saveButton.set ('disabled', disabled || !this.modified);
 			}
 			
 			if (this.overlayNode) {
 				domStyle.set (this.overlayNode, 'display', disabled ? '' : 'none');
 			}
 		},
 		
 		startup: function () {
 			this.inherited (arguments);
 			
 			this.tree.startup ();
 		},
 		
 		clear: function () {
 			if (this.tree) {
 				this.tree.clear ();
 			}
 		}
 	});
 });