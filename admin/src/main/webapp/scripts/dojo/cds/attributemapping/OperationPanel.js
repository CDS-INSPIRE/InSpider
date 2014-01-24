define ([
	'dojo/_base/declare',
	'dojo/_base/array',
	'dojo/query',
	
	'dojo/dom-construct',
	
	'./DndSource',
	
	'./Operation',
	
	'dijit/layout/ContentPane',
	
	'dijit/registry'
], function (declare, array, query, domConstruct, Source, Operation, ContentPane, registry) {

	return declare ([ContentPane], {
		content: '<div></div>',
		
		dndSourceNode: null,
		dndSource: null,
		
		postCreate: function () {
			this.inherited (arguments);
			
			this.dndSourceNode = query ('div', this.containerNode)[0];
			
			// Create drag&drop source:
			this.dndSource = new Source (this.dndSourceNode, {
				// Container:
				type: 'operation',
				
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
				generateText: false
			});
			
			domConstruct.place (new Operation ().domNode, this.dndSourceNode, 'last');
			domConstruct.place (new Operation ().domNode, this.dndSourceNode, 'last');
			domConstruct.place (new Operation ().domNode, this.dndSourceNode, 'last');
			
			this.dndSource.sync ();
		},
		
		uninitialize: function () {
			this.inherited (arguments);
		},
		
		_setOperationsAttr: function (/*Array*/operations) {
			array.forEach (registry.findWidgets (this.dndSourceNode), function (/*_WidgetBase*/widget) {
				widget.destroyRecursive ();
			});
			
			array.forEach (operations, function (/*Operation*/operation) {
				domConstruct.place (operation.domNode, this.dndSourceNode, 'last');
			}, this);
			
			this.dndSource.sync ();
		},
		
		_getOperationsAttr: function () {
			return registry.findWidgets (this.dndSourceNode);
		}
	});
});