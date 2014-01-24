define ([
	'dojo/_base/declare',
	'dojo/query',
	'dojo/_base/array',
	'dijit/registry',
	'dojo/_base/lang',
	
	'./Block',
	'./Placeholder',
	'./EmptyInput',
	
	'dijit/_WidgetBase',
	'dijit/_TemplatedMixin',
	
	'dojo/dom',
	'dojo/dom-class',
	'dojo/dom-construct',
	'dojo/dom-style',
	
	'dojo/text!./templates/AttributePanelRow.html'
], function (declare, query, array, registry, lang, Block, Placeholder, EmptyInput, WidgetBase, TemplatedMixin, dom, domClass, domConstruct, domStyle, rowTemplate) {
	return declare ([WidgetBase, TemplatedMixin], {
		baseClass: 'cdsAttributePanelRow',
		
		templateString: rowTemplate,
		
		tree: null,
		
		dndSource: null,
		
		blocks: null,
		
		postCreate: function () {
			this.inherited (arguments);
			
			this.blocks = [ ];
		},
		
		uninitialize: function () {
			this.inherited (arguments);
		},
		
		destroyDescendants: function () {
			this.inherited (arguments);

			console.log ('Destroying blocks in row');
			while (this.blocks.length > 0) {
				this.blocks.shift ().destroyRecursive ();
			}
		},
	
		createBlock: function (title, width, minInputs, fixed, cls) {
			if (!minInputs) {
				minInputs = 1;
			}
			
			var blockNode = domConstruct.create ('div', { }, this.containerNode, 'last'),
				block = new cls ({
					tree: this.tree,
					row: this,
					title: title,
					width: width,
					minInputs: minInputs,
					fixed: fixed
				}, blockNode);
			
			block.startup ();
			
			return block;
		},
		
		_getBlockPosition: function (/*Block*/block) {
			var blockPosition = 0;
			
			for (var i = 0; i < this.blocks.length; ++ i) {
				var b = this.blocks[i];
				
				if (b === block) {
					return blockPosition;
				}
				
				blockPosition += b.width;
			}
			
			return null;
		},
		
		_removeBlock: function (/*Block*/block, /*Number|Array?*/placeholderCount) {
			var placeholderWidths;
			
			if (placeholderCount.length) {
				placeholderWidths = placeholderCount;
				placeholderCount = placeholderWidths.length;;
			} else {
				if (placeholderCount === undefined || (placeholderCount > 0 && Math.floor (block.width / placeholderCount) < 1)) {
					placeholderCount = 1;
				}
				
				placeholderWidths = [ ];
				for (var i = 0; i < placeholderCount; ++ i) {
					placeholderWidths.push (block.width / placeholderCount);
				}
			}
			
			for (var i = 0; i < this.blocks.length; ++ i) {
				var b = this.blocks[i];
				
				if (b == block) {
					// Remove the block:
					this.containerNode.removeChild (b.domNode);
					b.set ({ row: null, tree: null });
					
					// Leave a placeholder:
					var placeholders = [ ];
					for (var j = placeholderCount - 1; j >= 0; -- j) {
						var placeholder = new Placeholder ({
								width: placeholderWidths[j],
								row: this,
								tree: this.tree
							});
						
						if (i > 0) {
							domConstruct.place (placeholder.domNode, this.blocks[i - 1].domNode, 'after');
						} else {
							domConstruct.place (placeholder.domNode, this.containerNode, 'first');
						}
						
						placeholders.unshift (placeholder);
					}

					this.blocks.splice.apply (this.blocks, [i, 1].concat (placeholders));
					
					return placeholders;
				}
			}
			
			return null;
		},
		
		_hasBlocks: function () {
			for (var i = 0; i < this.blocks.length; ++ i) {
				if (this.blocks[i].isInstanceOf (Block) || this.blocks[i].isInstanceOf (EmptyInput)) {
					return true;
				}
			}
			
			return false;
		},
		
		_getBlocksAtPosition: function (/*Number*/startPosition, /*Number*/width) {
			var position = 0,
				blocks = [ ];
			
			for (var i = 0; i < this.blocks.length && position < startPosition + width; ++ i) {
				var block = this.blocks[i];
				
				if (position >= startPosition) {
					blocks.push (block);
				}
				
				position += block.width;
			}
			
			return blocks;
		},
		
		_addBlock: function (/*Block*/block, /*Block*/reference, /*String*/direction) {
			for (var i = 0; i < this.blocks.length; ++ i) {
				var b = this.blocks[i];
				
				if (b !== reference) {
					continue;
				}

				if (direction == 'left') {
					this.blocks.splice (i, 0, block);
					domConstruct.place (block.domNode, reference.domNode, 'before');
				} else if (direction == 'right') {
					if (i == this.blocks.length - 1) {
						this.blocks.push (block);
					} else {
						this.blocks.splice (i + 1, 0, block);
					}
					domConstruct.place (block.domNode, reference.domNode, 'after');
				}
				
				block.set ('row', this);
				block.set ('tree', this.tree);
				
				if (!block._started) {
					block.startup ();
				}
				
				break;
			}
		},
		
		_splitPlaceholder: function (/*Block*/block) {
			for (var i = 0; i < this.blocks.length; ++ i) {
				var b = this.blocks[i];
				
				if (b !== block) {
					continue;
				}
				
				var width = block.width;
				block.set ('width', 1);

				var placeholders = [ ];
				for (var j = 0; j < width - 1; ++ j) {
					var placeholder = new Placeholder ({
						tree: this.tree,
						row: this,
						width: 1,
						solid: block.solid
					});
					
					placeholder.startup ();
					
					placeholders.push (placeholder);
					
					domConstruct.place (placeholder.domNode, block.domNode, 'before');
				}
				
				this.blocks.splice.apply (this.blocks, [i, 0].concat (placeholders));
				
				break;
			}
		},
		
		_replaceBlock: function (/*Block*/block, /*Block*/replacement, /*Boolean?*/dontDestroy) {
			console.log ('Row::_replaceBlock ', block.title, replacement.title);
			for (var i = 0; i < this.blocks.length; ++ i) {
				var b = this.blocks[i];
				
				if (b.isInstanceOf (EmptyInput)) {
					console.log ('Empty input: ', block, b);
				}
				
				if (b !== block) {
					continue;
				}
				
				console.log ('Replacing ', block.title, ' with ', replacement.title);
				domConstruct.place (replacement.domNode, block.domNode, 'after');
				this.blocks.splice (i, 1, replacement);
				if (!dontDestroy) {
					block.destroyRecursive ();
				} else {
					block.set ('tree', null);
					block.set ('row', null);
				}

				replacement.set ('tree', this.tree);
				replacement.set ('row', this);
				
				if (!replacement._started) {
					replacement.startup ();
				}
				
				break;
			}
			
			if (i == this.blocks.length) {
				console.log ('Block not found, block count: ', this.blocks.length);
			}
		},
		
		_appendBlock: function (/*Block*/block) {
			domConstruct.place (block.domNode, this.containerNode, 'last');
			this.blocks.push (block);
			
			block.set ('tree', this.tree);
			block.set ('row', this);
			
			if (!block._started) {
				block.startup ();
			}
		},
		
		_reserveSpace: function (/*Number*/position, /*Number*/width) {
			var currentPosition = 0,
				block = null;
			
			for (var i = 0; i < this.blocks.length; ++ i) {
				block = this.blocks[i];
				
				if (currentPosition == position) {
					var placeholder = new Placeholder ({
						width: width,
						tree: this.tree,
						row: this,
						solid: false
					}); 
					placeholder.startup ();
					this._addBlock (placeholder, block, 'left');
					
					return;
				}
				
				currentPosition += block.width;
			}
			
			if (currentPosition == position && block) {
				var placeholder = new Placeholder ({
					width: width,
					tree: this.tree,
					row: this,
					solid: false
				});
				placeholder.startup ();
				this._addBlock (placeholder, block, 'right');
			}
		}
	});
});