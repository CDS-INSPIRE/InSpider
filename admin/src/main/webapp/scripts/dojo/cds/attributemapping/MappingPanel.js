define ([
         'dojo/_base/declare',
         'dojo/_base/array',
         'dijit/layout/BorderContainer',
         'dijit/layout/AccordionContainer',
         'dijit/layout/ContentPane',
         'dijit/layout/TabContainer',
         'cds/attributemapping/AttributePanel',
         './OperationPanel',
         './ConditionPanel',
         'dojo/dom-class',
         'dojo/dom-prop',
         'dojo/dom-attr',
         'dojo/dom-construct',
         'dijit/form/Button',
         'dojo/query',
         'dojo/_base/lang',
         'dojox/html/entities',
         'dojo/on',
         './PreviewWindow',

         'dojo/text!./templates/MappingPanelHeader.html',
         'dojo/text!./templates/FilterConditiesMockup.html',
         
         'dojo/NodeList-dom'
], function (
		declare, 
		array, 
		BorderContainer, 
		AccordionContainer, 
		ContentPane, 
		TabContainer, 
		AttributePanel, 
		OperationPanel, 
		ConditionPanel,
		domClass, 
		domProp, 
		domAttr,
		domConstruct,
		Button, 
		query,
		lang, 
		htmlEntities, 
		on,
		PreviewWindow,
		
		mappingPanelHeader,
		conditionsMockup) {
	return declare ([BorderContainer], {
		headerContainer: null,
		treePanel: null,
		sidebarContainer: null,
		inputContainer: null,
		inputPane: null,
		operationContainer: null,
		operationPane: null,
		backButton: null,
		helpButton: null,
		loadingPanel: null,
		conditionPanel: null,
		mappingPanel: null,
		previewPanel: null,
		errorPanel: null,
		
		panels: null,
		
		loading: false,
		error: false,
		
		datasetName: 'onbekend',
		themeName: 'onbekend',
		bronhouderName: 'onbekend',
		
		postCreate: function () {
			this.inherited (arguments);

			// Create header:
			this.headerContainer = new ContentPane ({
				splitter: false,
				region: 'top',
				gutters: false,
				layoutPriority: 1,
				content: mappingPanelHeader
			});
			domClass.add (this.headerContainer.containerNode, 'cdsMappingPanelHeader');
			
			this.addChild (this.headerContainer);

 			this.backButton = new Button ({
 				label: 'Terug',
 				title: 'Keer terug naar het overzicht van datasets.',
 				iconClass: 'icon-back'
 			}, query ('span.cdsMappingPanelBackButton', this.headerContainer.containerNode)[0]);

 			domProp.set (query ('.cdsMappingPanelDatasetName', this.headerContainer.containerNode)[0], 'innerHTML', htmlEntities.encode (this.datasetName));
 			domProp.set (query ('.cdsMappingPanelThemeName', this.headerContainer.containerNode)[0], 'innerHTML', htmlEntities.encode (this.themeName));
 			domProp.set (query ('.cdsMappingPanelBronhouderName', this.headerContainer.containerNode)[0], 'innerHTML', htmlEntities.encode (this.bronhouderName));
			
 			this.own (this.backButton.on ('click', lang.hitch (this, function () {
 				this.emit ('back', {
 					bubbles: true,
 					attributePanel: this
 				});
 			})));
 			
 			// Create the mapping panel:
 			this.mappingPanel = new BorderContainer ({
 				splitter: false,
 				region: 'center',
 				gutters: false,
 				layoutPriority: 1
 			});
 			domClass.add (this.mappingPanel.domNode, 'cdsMappingPanelChild');
 			
 			// Create mapping panel content:
			this.sidebarContainer = new BorderContainer ({
				splitter: true,
				region: 'right',
				gutters: false,
				layoutPriority: 1,
				style: 'width: 25em;'
			});
			
			this.mappingPanel.addChild (this.sidebarContainer);
			
			this.treePanel = new TabContainer ({
				region: 'center',
				tabPosition: 'top',
	     		tabPosition: 'left-h',
	     		//nested: true,
				title: 'Attribuutmapping',
				style: 'padding: 0px;'
			});
			
			this.mappingPanel.addChild (this.treePanel);
			
			this.inputContainer = new AccordionContainer ({
				splitter: true,
				region: 'top',
				gutters: false,
				layoutPriority: 1,
				style: 'height: 300px'
			});
			this.operationContainer = new AccordionContainer ({
				splitter: true,
				region: 'center',
				gutters: false,
				layoutPriority: 1
			});
			
			this.sidebarContainer.addChild (this.inputContainer);
			this.sidebarContainer.addChild (this.operationContainer);
			
			this.inputPane = new OperationPanel ({
				title: 'Invoerattributen'
			});
			this.operationPane = new OperationPanel ({
				title: 'Operaties'
			});
			
			this.inputContainer.addChild (this.inputPane);
			this.operationContainer.addChild (this.operationPane);
			
			// Create loading and error panels:
			this.loadingPanel = new ContentPane ({
				region: 'center',
				content: '<div class="cdsMappingPanelLoading"><h2>Bezig met laden condities en attribuutmapping ...</h2><p>Het applicatieschema en de bestaande condities en attribuutmapping wordt opgehaald bij de server.</p></div>'
			});
			this.errorPanel = new ContentPane ({
				region: 'center',
				content: '<div class="cdsMappingPanelError"><h2>Fout bij het laden condities en attribuutmapping.</h2><p>Het GML applicatieschema is mogelijk ongeldig, of er is een fout opgetreden bij de communicatie met de server.</p><p class="message"></p></div>'
			});
			
			// Create condition panel:
			this.conditionPanel = new ConditionPanel ({
				region: 'center'
			});
			this.previewPanel = new PreviewWindow ({
				region: 'center'
			});
			
			domClass.add (this.conditionPanel.domNode, 'cdsMappingPanelChild');
			domClass.add (this.previewPanel.domNode, 'cdsMappingPanelChild');
			
			this.panels = {
				conditions: [ this.conditionPanel ],
				mapping: [ this.mappingPanel ],
				preview: [ this.previewPanel ]
			};
			
			this._createMenu ();
			this._setLoadingAttr (this.loading);
			this._setErrorAttr (this.error);
			
			// Listen for resize events. These are fired when attribute panels need to be
			// resized (for example when the label or icon in the tab changes).
			this.own (this.on ('resize', lang.hitch (this, function () {
				this.resize ();
			})));
			
			// Reset the iconClass on the tab for each attribute. For some reason the iconClass isn't applied
			// in Chrome when the panel is not added to the DOM.
			this.mappingPanel.onChildAdded = lang.hitch (this, function () {
				array.forEach (this.treePanel.getChildren (), function (child) {
					var iconClass = child.get ('iconClass');
					child.set ('iconClass', 'dijitNoIcon');
					child.set ('iconClass', iconClass);
				});
			});

		},
		
		startup: function () {
			this.inherited (arguments);
			
			// Startup panels:
			for (var i in this.panels) {
				array.forEach (this.panels[i], function (panel) {
					panel.startup ();
				});
			}
		},
		
		_createMenu: function () {
			array.forEach (query ('.cdsMappingPanelMenu a', this.headerContainer.containerNode), function (node) {
				var panelName = domAttr.get (node, 'data-panel'),
					parentNode = node.parentNode;
				
				if (!panelName) {
					return;
				}
				
				on (node, 'click', lang.hitch (this, function (e) {
					e.preventDefault ();
					
					if (!domClass.contains (parentNode, 'active')) {
						this.emit ('selectpanel', {
							bubbles: true,
							panel: panelName
						});
					}
					
					for (var i in this.panels) {
						if (i == panelName) {
							continue;
						}
						array.forEach (this.panels[i], function (p) { this.removeChild (p); }, this);
					}
					for (var i in this.panels) {
						if (i != panelName) {
							continue;
						}
						array.forEach (this.panels[i], function (p) { 
							this.addChild (p);
							if (p.onChildAdded) {
								p.onChildAdded ();
							}
						}, this);
					}
					
					query ('.cdsMappingPanelMenu li', this.headerContainer.containerNode).removeClass ('active');
					domClass.add (parentNode, 'active');
				}));
			}, this);
			
			domClass.add (query ('.cdsMappingPanelMenu li')[0], 'active');
		},
		
		_clearAttributes: function () {
			// Remove all tabs:
			array.forEach (this.treePanel.getChildren (), function (/*AttributePanel*/panel) {
				this.treePanel.removeChild (panel);
				panel.destroyRecursive ();
			}, this);
		},

		_setLoadingAttr: function (/*Boolean*/loading) {
			this._set ('loading', loading);
			this._setLoadingError ();
		},
		
		_setErrorAttr: function (/*Boolean*/error) {
			this._set ('error', error);
			this._setLoadingError ();
		},
		
		_setErrorMessageAttr: function (/*String*/errorMessage) {
			this._set ('errorMessage', errorMessage);
			var node = query ('p.message', this.errorPanel.domNode)[0];
			
			console.log ('Setting error message: ', errorMessage, node);
			domConstruct.empty (node);
			domConstruct.place (document.createTextNode (errorMessage), node, 'last');
		},
		
		_setLoadingError: function () {
			if (!this.mappingPanel) {
				return;
			}
			if (this.error) {
				this.removeChild (this.mappingPanel);
				this.removeChild (this.conditionPanel);
				this.removeChild (this.previewPanel);
				this.removeChild (this.loadingPanel);
				this.addChild (this.errorPanel);
			} else if (this.loading) {
				this.removeChild (this.mappingPanel);
				this.removeChild (this.errorPanel);
				this.removeChild (this.conditionPanel);
				this.removeChild (this.previewPanel);
				this.addChild (this.loadingPanel);
			} else {
				this.removeChild (this.errorPanel);
				this.removeChild (this.loadingPanel);
				this.removeChild (this.mappingPanel);
				this.removeChild (this.previewPanel);
				this.addChild (this.conditionPanel);
			}
		},

		_setFeatureTypeAttr: function (/*Object*/featureType) {
			this.conditionPanel.set ('featureType', featureType);
		},
		
		_getFeatureTypeAttr: function () {
			return this.conditionPanel.get ('featureType');
		},
		
		_setInputsAttr: function (/*Array*/operations) {
			this.inputPane.set ('operations', operations);
		},
		
		_getInputsAttr: function () {
			this.inputPane.get ('operations');
		},
		
		_setOperationsAttr: function (/*Array*/operations) {
			this.operationPane.set ('operations', operations);
		},
		
		_getOperationsAttr: function () {
			return this.operationPane.get ('operations');
		},
		
		_setAttributesAttr: function (/*Array*/attributes) {
			this._clearAttributes ();
			array.forEach (attributes, function (attr) {
				this.treePanel.addChild (attr);
			}, this);
			this._set ('attributes', attributes);
		}
	});
});