define ([
	'dojo/_base/declare',
	'dojo/query',
	'dojo/_base/array',
	'dijit/registry',
	'dojo/_base/lang',
	
	'./_CleanupTreeMixin',
	'./_ValidateTreeMixin',
	'./_EmptyInputsTreeMixin',
	'./_UpdateBlockInputsMixin',
	'./_NewCleanupTreeMixin',
	
	'./Placeholder',
	'./EmptyInput',
	'./Block',
	'./Row',
	'./Tree',
	'./ConditionRow',
	'./ConditionBlock',
	'./WorkQueue',

	'dojo/dom',
	'dojo/dom-class',
	'dojo/dom-construct',
	'dojo/dom-style',
	
	'dijit/_WidgetBase',
	
	'dojo/Deferred',
	
	'dijit/Dialog',
	'./form/PropertiesForm'
], function (
		declare, 
		query, 
		array, 
		registry, 
		lang, 
		CleanupTreeMixin, 
		ValidateTreeMixin,
		EmptyInputsTreeMixin,
		UpdateBlockInputsMixin,
		NewCleanupTreeMixin,
		Placeholder, 
		EmptyInput, 
		Block, 
		Row, 
		Tree, 
		ConditionRow, 
		ConditionBlock, 
		WorkQueue, 
		dom, 
		domClass, 
		domConstruct, 
		domStyle, 
		WidgetBase, 
		Deferred,
		Dialog,
		PropertiesForm) {
	
	return declare ([WidgetBase, /*CleanupTreeMixin, EmptyInputsTreeMixin,*/NewCleanupTreeMixin, ValidateTreeMixin, UpdateBlockInputsMixin], {
		elementWidth: 16,
		elementUnit: 'em',
		
		headerNode: null,
		rowsNode: null,
		
		rows: null,

		workQueue: null,
		
		modified: false,
		
		constructor: function () {
			this.workQueue = new WorkQueue ();
			this.rows = [ ];
		},
		
		/**
		 * Sets new content for this tree. This operation is performed asynchronously after the current
		 * task completes.
		 */
		setBlocks: function (/*Array*/rows) {
			var d = new Deferred ();
			// Flush all tasks from the work queue:
			this.workQueue.flush ();
			
			// Schedule the tree to be full cleared:
			this.clear ();
			
			// Add a new tasks to the work queue:
			this.workQueue.push (lang.hitch (this, function () {
				this.rows = [ ];
				
				array.forEach (rows, function (/*Object*/row) {
					var rowClass = Row,
						blocks;
					
					if (row.rowClass) {
						rowClass = row.rowClass;
					}
					if (row.blocks) {
						blocks = row.blocks;
					} else if (row.length) {
						blocks = row;
					} else {
						blocks = [ ];
					}
					
					var newRow = this._createRow (rowClass);
					this.rows.push (newRow);
					
					array.forEach (blocks, function (/*Block*/block) {
						newRow._appendBlock (block);
					}, this);
				}, this);
			}));
			this._scheduleCleanupTasks ();
			this.workQueue.push ({ key: 'resetModified', task: lang.hitch (this, function () {
				this.workQueue.push (lang.hitch (this, function () {
					this.set ('modified', false);
					d.resolve ();
				}));
			})} );
			
			return d;
		},
		
		/**
		 * Removes the given block from the tree. Blocks are removed after all previously scheduled tasks
		 * have completed.
		 * 
		 * @return A deferred that is resolved when the block is removed from the tree.
		 */
		removeBlock: function (/*Block*/block) {
			// Schedule the block for removal:
			this.workQueue.push ({
				task: lang.hitch (this, function () { this._removeBlock (block); }),
				key: block
			});

			// Schedule cleanup tasks to be performed after removing the block:
			this._scheduleCleanupTasks ();
			this._scheduleModify ();
			
			// Fire the deferred at the end of the queue:
			return this.workQueue.mark ();
		},
		
		/**
		 * Replaces the given block with a new replacement block. If the replacement is
		 * currently in the tree it is removed first. Blocks are replaced after all previously
		 * scheduled tasks have finished.
		 * 
		 * @return A deferred that fires when the block has been replaced in the tree anv
		 *   validation is complete.
		 */
		replaceBlock: function (/*Block*/original, /*Block*/replacement) {
			// Do nothing when original and replacement are the same blocks:
			if (original === replacement) {
				return;
			}
			
			console.log ('replaceBlock: ', original.title, replacement.title);
			
			// Schedule removal of the replacement block if it was previously in the tree:
			if (replacement.tree) {
				this.removeBlock (replacement);
			}

			// Schedule the replace operation:
			this.workQueue.push ({
				task: lang.hitch (this, function () {
					this._replaceBlock (original, replacement);
				}),
				key: original
			});
			
			// Use cleanup to update the "solid" flags of the blocks in the tree and
			// to remove any empty inputs that are no longer needed because the minimum number
			// of inputs is now met:
			this._scheduleCleanupTasks ();
			this._scheduleModify ();
			
			return this.workQueue.mark ();
		},
		
		_replaceBlock: function (/*Block*/original, /*Block*/replacement) {
			if (original.row) {
				original.row._replaceBlock (original, replacement);
			}
		},

		/**
		 * Adds a block in the tree with respect to a given reference block. Blocks
		 * are inserted after all previously scheduled tasks have finished. If the reference
		 * block is no longer in the tree at this point, the operation is cancelled. If the
		 * block is previously at a different position in the tree it is removed from that
		 * position first.
		 * 
		 * @return A deferred that fires when the block has been added and the tree
		 *   has been validated.
		 */
		addBlock: function (/*Block*/block, /*Block*/reference, /*String*/direction) {
			// Do nothing when adding the block to itself:
			if (block === reference) {
				return;
			}
			
			// Schedule this block's removal if it was previously in the tree:
			if (block.tree) {
				this.removeBlock (block);
			}

			// Schedule the 'add' operation:
			this.workQueue.push ({
				task: lang.hitch (this, function () {
					this._addBlock (block, reference, direction);
				}),
				key: block
			});
			
			// Use cleanup to update the "solid" flags of the blocks in the tree and
			// to remove any empty inputs that are no longer needed because the minimum number
			// of inputs is now met:
			this._scheduleCleanupTasks ();
			this._scheduleModify ();
			
			// Fire a deferred after completion:
			return this.workQueue.mark ();
		},
		
		_addBlock: function (/*Block*/block, /*Block*/reference, /*String*/direction) {
			console.log ('adding');
			if (!reference.tree) {
				return;
			}
			
			block.set ('width', 1);
			
			if (direction == 'right' || direction == 'left') {
				this._addBlockLeftRight (block, reference, direction);
			} else if (direction == 'bottom') {
				this._addBlockBelow (block, reference);
			} else if (direction == 'top') {
				this._addBlockAbove (block, reference);
			}
		},
		
		/**
		 * Adds a new condition to the tree. The condition is added after al previously scheduled
		 * tasks have completed.
		 * 
		 * @return A deferred that fires when the condition has been added and the tree has been validated.
		 */
		addCondition: function (/*ConditionBlock?*/newBlock) {
			// Locate the condition row and return if it doesn't exist or if it is empty:
			var row = this._getConditionRow ();
			if (!row || row.blocks.length <= 0) {
				return;
			}

			if (!newBlock) {
				newBlock = new ConditionBlock ({
					tree: this,
					row: row,
					fixed: true,
					noInputs: false,
					noTypes: true,
					canEdit: true,
					canDelete: true,
					lastCondition: false,
					solid: true,
					minInputs: 1,
					width: 1,
					typeDescription: 'Conditie',
					validate: lastBlock.validate,
					data: { }
				});
			}
			
			// Add a new block:
			var lastBlock = row.blocks.length > 0 ? row.blocks[row.blocks.length - 1] : null;

			if (lastBlock) {
				newBlock.validate = lastBlock.validate;
			}
			
			// Schedule the block to be added:
			this.addBlock (newBlock, lastBlock, 'left');
			
			// Fire a deferred:
			return this.workQueue.mark ();
		},

		/**
		 * Removes the given condition from the tree. Removing a condition removes
		 * all child blocks from the tree as well.
		 * 
		 * @return A deferred that fires when the condition has been removed from the tree.
		 */
		removeCondition: function (/*ConditionBlock*/block) {
			var d = new Deferred ();
			
			// Schedule the remove operation:
			this.workQueue.push ({
				task: lang.hitch (this, function () {
					if (!block.tree) {
						return;
					}
					
					var children = [ ],
						fringe = [ block ];
					
					while (fringe.length > 0) {
						var b = fringe.shift (),
							c = this._getBlockInputs (b);
						
						for (var i = 0; i < c.length; ++ i) {
							fringe.push (c[i]);
						}
						
						children.unshift (b);
					}
					
					for (var i = 0; i < children.length; ++ i) {
						console.log ('Scheduling block for removal: ', children[i]);
						this._removeBlock (children[i]);
						children[i].destroyRecursive ();
						//this.workQueue.push (lang.hitch (children[i], function () { this.destroyRecursive (); }));
					}
					
					this._scheduleCleanupTasks ();
					this._scheduleModify ();
					this.workQueue.push (function () { d.resolve (); });
				}),
				key: block
			});
			
			return d;
		},

		/**
		 * Clears the tree: removes all blocks.
		 * 
		 * @return A deferred that fires after all blocks have been removed.
		 */
		clear: function () {
			this.workQueue.push (lang.hitch (this, function () {
				while (this.rows.length > 0) {
					this._removeRow (this.rows[0]);
				}
			}));

			this._scheduleCleanupTasks ();
			this._scheduleModify ();
			
			return this.workQueue.mark ();
		},

		/**
		 * Returns all blocks that are currently in the tree. Blocks are returned after all
		 * tasks that are currently pending have been completed.
		 * 
		 * @return A deferred that fires when the blocks list becomes available.
		 */
		getBlocks: function () {
			var d = new Deferred ();
			
			this.workQueue.push (lang.hitch (this, function () {
				d.resolve (array.map (this.rows, function (/*Row*/row) {
					return array.map (row.blocks, function (/*_BlockMixin*/block) {
						return block;
					}, this);
				}, this));
			}));
			
			return d;
		},
		
		_createRow: function (cls) {
			if (!cls) {
				cls = Row;
			}

			var container = cls === ConditionRow ? this.headerNode : this.rowsNode;
			
			var rowNode = domConstruct.create ('div', { }, container, 'last'),
				row = new cls ({
					tree: this
				}, rowNode);
			
			return row;
		},
		
		buildRendering: function () {
			this.inherited (arguments);
			
			this.headerNode = domConstruct.create ('div', {
				
			}, this.domNode, 'last');
			
			this.rowsNode = domConstruct.create ('div', {
				
			}, this.domNode, 'last');
		},
		
		startup: function () {
			this.inherited (arguments);
		},
		
		_removeBlock: function (/*Block*/block) {
			if (!block.tree || !block.row) {
				return;
			}
			
			var row = block.row,
				rowIndex = this._getRowIndex (row),
				blockPosition = row._getBlockPosition (block),
				inputs = this._getBlockInputs (block, rowIndex, blockPosition);
			
			// Split the parent placeholders if this block has more than 1 input:
			if (inputs.length > 1) {
				var parent = this._getParentBlock (rowIndex, blockPosition);
				while (parent && parent.block.isInstanceOf (Placeholder)) {
					parent.row._splitPlaceholder (parent.block);
					parent = this._getParentBlock (parent.rowIndex, parent.position);
				}
			}
			
			// Remove block from its row:
			var placeholderWidths = [ ];
			for (var i = 0; i < inputs.length; ++ i) {
				placeholderWidths.push (inputs[i].width);
			}
			if (placeholderWidths.length == 0) {
				placeholderWidths.push (1);
			}
			var placeholders = row._removeBlock (block, placeholderWidths);
			
			// If no other blocks on the same row remain:
			if (!row._hasBlocks ()) {
				// Remove the entire row:
				console.log ('Row has no more blocks, removing');
				this._removeRow (row);
			}
		},
		
		_getConditionRow: function () {
			for (var i = 0; i < this.rows.length; ++ i) {
				var row = this.rows[i];
				if (row.isInstanceOf (ConditionRow)) {
					return row;
				}
			}
			return null;
		},
		
		_addBlockLeftRight: function (/*Block*/block, /*Block*/reference, /*String*/direction) {
			// Reserve room in each parent block:
			var parent = this._getParentBlock (this._getRowIndex (reference.row), reference.row._getBlockPosition (reference));
			while (parent) {
				parent.block.set ('width', parent.block.width + block.width);
				parent = this._getParentBlock (parent.rowIndex, parent.position);
			}

			// Add the block next to the reference:
			reference.row._addBlock (block, reference, direction);
			
			// Split each placeholder parent:
			var parent = this._getParentBlock (this._getRowIndex (block.row), block.row._getBlockPosition (block));
			while (parent && parent.block.isInstanceOf (Placeholder) && parent.block.width > 1) {
				parent.row._splitPlaceholder (parent.block);
				parent = this._getParentBlock (parent.rowIndex, parent.position);
			}
			
			// Insert a placeholder in each row above this row:
			var rowIndex = this._getRowIndex (block.row),
				blockPosition = block.row._getBlockPosition (block);

			for (var i = 0; i < rowIndex; ++ i) {
				this.rows[i]._reserveSpace (blockPosition, block.width);
			}
		},
		
		_addBlockAbove: function (/*Block*/block, /*Block*/reference) {
			// See if there is room above the reference, if so replace it:
			var inputs = this._getBlockInputs (reference);
			if (inputs.length == 1 && (inputs[0].isInstanceOf (Placeholder) || inputs[0].isInstanceOf (EmptyInput))) {
				inputs[0].row._replaceBlock (inputs[0], block);
				return;
			}
			
			// There is no room, insert a new row above the reference row:
			var rowIndex = this._getRowIndex (reference.row),
				referencePosition = reference.row._getBlockPosition (reference),
				referenceWidth = reference.width,
				copyRow = this.rows[rowIndex > 0 ? rowIndex - 1 : rowIndex],
				row = new Row ({ tree: this });
			
			// When inserting before a condition row (which is positioned in the header), insert
			// before the last row instead:
			if (reference.row.isInstanceOf (ConditionRow)) {
				domConstruct.place (row.domNode, this.rows[this.rows.length - 1].domNode, 'before');
			} else {
				domConstruct.place (row.domNode, reference.row.domNode, 'before');
			}
			
			this.rows.splice (this._getRowIndex (reference.row), 0, row);
			
			var position = 0;
			array.forEach (copyRow.blocks, function (/*Block*/b) {
				if (position == referencePosition) {
					block.set ('width', referenceWidth);
					row._appendBlock (block);
				} else if (position < referencePosition || position >= referencePosition + referenceWidth) {
					row._appendBlock (new Placeholder ({
						width: b.width,
						solid: false,
						tree: this,
						row: row
					}));
				}
				position += b.width;
			}, this);
		},
		
		_addBlockBelow: function (/*Block*/block, /*Block*/reference) {
			// See if there is room below the reference:
			var parent = this._getParentBlock (this._getRowIndex (reference.row), reference.row._getBlockPosition (reference));
			if (parent && parent.block.isInstanceOf (Placeholder)) {
				console.log ('Replacing placeholder at: ', parent.rowIndex, parent.position);
				parent.block.row._replaceBlock (parent.block, block);
				return;
			}
			
			// There is no room, insert a new row below the reference row:
			var row = new Row ({
				tree: this
			});
			domConstruct.place (row.domNode, reference.row.domNode, 'after');
			this.rows.splice (this._getRowIndex (reference.row) + 1, 0, row);

			array.forEach (reference.row.blocks, function (/*Block*/b) {
				if (b === reference) {
					block.set ('width', b.width);
					row._appendBlock (block);
				} else {
					row._appendBlock (new Placeholder ({
						width: b.width,
						solid: b.solid,
						tree: this,
						row: row
					}));
				}
			}, this);
		},
		
		_getBlockInputs: function (/*Block*/block, /*Number?*/rowIndex, /*Number?*/blockPosition) {
			var row = block.row;
			
			if (rowIndex === undefined) {
				rowIndex = this._getRowIndex (row);
			}
			if (blockPosition === undefined) {
				blockPosition = row._getBlockPosition (block);
			}
			
			if (rowIndex <= 0) {
				return [ ];
			}
			
			return this.rows[rowIndex - 1]._getBlocksAtPosition (blockPosition, block.width);
		},
		
		_getNonPlaceholderBlockInputs: function (/*Block*/block, /*Number?*/rowIndex, /*Number*/blockPosition) {
			return array.filter (this._getBlockInputs (block, rowIndex, blockPosition), function (b) {
				return !b.isInstanceOf (Placeholder);
			});
		},
		
		_getRowIndex: function (/*Row*/row) {
			for (var i = 0; i < this.rows.length; ++ i) {
				if (this.rows[i] === row) {
					return i;
				}
			}
			
			return null;
		},
		
		_removeRow: function (/*Row*/row) {
			console.log ('Removing, looking for row ', row);
			for (var i = 0; i < this.rows.length; ++ i) {
				var r = this.rows[i];
				
				console.log (r);
				if (r === row) {
					console.log ('Removing row ', row);
					this.rows.splice (i, 1);
					r.destroyRecursive ();
				}
			}
		},

		_getParentBlock: function (/*Number*/rowIndex, /*Number*/position) {
			if (rowIndex >= this.rows.length - 1) {
				return null;
			}
			
			var row = this.rows[rowIndex + 1],
				blocks = row.blocks,
				rowPosition = 0;
			
			for (var i = 0; i < blocks.length; ++ i) {
				var block = blocks[i];
				
				if (position >= rowPosition && position < rowPosition + block.width) {
					return {
						block: block,
						position: rowPosition,
						rowIndex: rowIndex + 1,
						row: row
					};
				}
				
				rowPosition += block.width;
			}
			
			return null;
		},
		
		_swapBlocks: function (/*Block*/a, /*Block*/b, /*Array?*/placeholderWidths) {
			var placeholder = new Placeholder ({
				solid: true,
				width: a.width
			});
			
			// Replace a with a placeholder, keeping a unlinked:
			a.row._replaceBlock (a, placeholder, true);
			
			// Replace b with a, keeping b unlinked:
			b.row._replaceBlock (b, a, true);
			
			// Replace a (placeholder) with b, destroying the placeholder:
			placeholder.row._replaceBlock (placeholder, b, false);
			
			if (placeholderWidths) {
				b.row._removeBlock (b, placeholderWidths);
			}
		},
		
		_scheduleCleanupTasks: function () {
			var removedTasks = this.workQueue.cancel ('resetModified');
			
			this._doScheduleCleanupTasks ();
			
			this.workQueue.push (removedTasks);
		},
		
		_doScheduleCleanupTasks: function () {
			this.inherited (arguments);
		},
		
		_scheduleModify: function () {
			this.workQueue.push ({
				task: lang.hitch (this, function () {
					this.set ('modified', true);
				}),
				key: 'modify'
			});
		},
		
		_onEditBlock: function (/*Block*/block) {
			this.onEditBlock (block, this);
			
			this._createEditDialog ();
			
			console.log ('editing block ', block.data.settings);
			
			this.currentEditBlock = block;
			this.editForm.set ('values', block.data.settings);
			this.editDialog.set ('title', 'Eigenschappen: ' + block.label);
			this.editDialog.show ();
		},
		
		onEditBlock: function (/*Block*/block, /*Tree*/tree) { },
		
		_createEditDialog: function () {
			if (this.editDialog) {
				return;
			}
			
			var editDialog = this.editDialog = new Dialog ({
				title: 'Eigenschappen',
				content: '<div></div>',
				style: 'width: 400px'
			});
			
			var editForm = this.editForm = new PropertiesForm ({
			}, query ('div', this.editDialog.containerNode)[0]);
			
			editForm.on ('Close', function () {
				editDialog.hide ();
			});
			editForm.on ('modified', lang.hitch (this, function () {
				this.set ('modified', true);
			}));
			editDialog.on ('Hide', lang.hitch (this, function () {
				if (this.currentEditBlock) {
					this.currentEditBlock._updateTitle ();
					this.currentEditBlock = null;
				}
			}));
			
			editDialog.startup ();
			editForm.startup ();
		}
	});
});