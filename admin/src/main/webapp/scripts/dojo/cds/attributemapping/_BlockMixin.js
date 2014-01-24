define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/_base/array',
	'dijit/_WidgetBase',
	'dijit/registry',
	'dojo/dom-style',
	'dojo/dom-class',
	'dojo/dnd/Target'
], function (declare, lang, array, WidgetBase, registry, domStyle, domClass, Target) {
	return declare ([WidgetBase], {
		row: null,
		tree: null,
		width: 1,
		solid: false,
		minInputs: 0,

		dndTargets: null,
		
		uninitialize: function () {
			this.inherited (arguments);
			
			if (this.dndTargets) {
				array.forEach (this.dndTargets, function (target) {
					target.destroy ();
				});
				this.dndTargets = null;
			}
		},
		
		destroy: function () {
			this.inherited (arguments);
			
			this.row = null;
			this.tree = null;
		},
		
		_setWidthAttr: function (/*Number*/width) {
			this.width = width;
			
			if (!this.tree) {
				return;
			}
			
			var units = this.tree.elementWidth * width,
				unit = this.tree.elementUnit;
			
			domStyle.set (this.domNode, {
				width: ('' + units) + unit
			});
		},
		
		_setSolidAttr: function (/*Boolean*/solid) {
			this.solid = solid;
			
			domClass[solid ? 'add' : 'remove'] (this.domNode, 'solid');
		},
		
		_setTreeAttr: function (/*Tree*/tree) {
			this.tree = tree;
			this._setWidthAttr (this.width);
		},
		
		createTarget: function (/*DOMNode*/node, /*String*/direction) {
			var self = this,
				targetNode = node;
			var target = new Target (node, {
				isSource: false,
				onDrop: function (source, nodes, copy) {
					if (nodes.length < 1) {
						return;
					}
					
					var node = nodes[0];
					
					console.log ('Dropping node: ', node);
					
					while (node && !(domClass.contains (node, 'cdsAttributePanelBlock') || domClass.contains (node, 'cdsOperation'))) {
						node = node.parentNode;
					}
					
					if (!node) {
						return;
					}
					
					var object = registry.byNode (node);
					
					if (!object) {
						return;
					}
					
					var block;
					if (object.blockFactory) {
						block = object.blockFactory ();
					} else {
						block = object;
					}
					
					if (!block) {
						return;
					}
					
					self._dropBlock (block, direction);
					
					array.forEach (['left', 'right', 'top', 'bottom'], function (e) {
						domClass.remove (targetNode, self.baseClass + 'Over-' + e);
						domClass.remove (self.domNode, self.baseClass + 'Over-' + e);
					});
				}
			}) ;

			target.on ('DraggingOver', lang.hitch (this, function () {
				domClass.add (node, this.baseClass + 'Over');
				domClass.add (this.domNode, this.baseClass + 'Over-' + direction);
			}));
			target.on ('DraggingOut', lang.hitch (this, function () {
				domClass.remove (node, this.baseClass + 'Over');
				domClass.remove (this.domNode, this.baseClass + 'Over-' + direction);
			}));
			
			return target;
		},
		
		_dropBlock: function (/*Block*/block, /*String*/direction) {
		}
	});
});