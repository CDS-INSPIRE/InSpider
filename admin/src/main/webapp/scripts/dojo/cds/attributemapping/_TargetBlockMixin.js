define ([
	'dojo/_base/declare',
	'dojo/dnd/Target',
	'dojo/dom-class',
	'dijit/registry'
], function (declare, Target, domClass, registry) {
	
	return declare ([], {
		dndTargetNode: null,
		dndTarget: null,
		
		postCreate: function () {
			this.inherited (arguments);

			if (!this.dndTargetNode) {
				return;
			}
			
			var self = this;
			this.dndTarget = new Target (this.dndTargetNode, {
				isSource: false,
				onDrop: function (source, nodes, copy) {
					console.log ('onDrop ', source, nodes, copy);
					
					if (nodes.length < 1) {
						return;
					}
					
					var node = nodes[0];
					
					while (node && !domClass.contains (node, 'cdsAttributePanelBlock')) {
						node = node.parentNode;
					}
					
					if (!node) {
						return;
					}
					
					console.log ('Invoking handler');
					self._dropBlock (registry.byNode (node));
					console.log ('Handler returned');
				}
			});
		},
		
		uninitialize: function () {
			this.inherited (arguments);
			
			if (this.dndTarget) {
				this.dndTarget.destroy ();
				this.dndTarget = null;
			}
		}, 
		
		_dropBlock: function (/*Block*/block) { }
	});

});