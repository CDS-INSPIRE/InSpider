define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/_base/array',
	'dojo/Deferred',
	'./asyncIterator',
	
	'./Block',
	'./EmptyInput',
	'./Placeholder'
], function (declare, lang, array, Deferred, asyncIterator, Block, EmptyInput, Placeholder) {
	return declare (null, {
		performingCleanup: false,
		rerunCleanup: false,
		
		_doScheduleCleanupTasks: function () {
			this.inherited (arguments);
			
			// Only start a cleanup action if the current cleanup operation has finished:
			if (this.performingCleanup) {
				return;
			}
			
			console.log ('_doScheduleCleanupTasks');

			this.workQueue.push ({
				task: lang.hitch (this, function () { this.performingCleanup = true; }),
				key: 'startCleanup'
			});
			this.workQueue.push ({
				task: lang.hitch (this, this._removeEmptyInputs),
				key: 'removeEmptyInputs'
			});
			this.workQueue.push ({
				task: lang.hitch (this, this._pushDown),
				key: 'pushDown'
			});
			this.workQueue.push ({
				task: lang.hitch (this, this._cleanupPlaceholders),
				key: 'cleanupPlaceholders'
			});
			this.workQueue.push ({
				task: lang.hitch (this, this._updateColumnWidth),
				key: 'updateColumnWidth'
			});
			this.workQueue.push ({
				task: lang.hitch (this, this._addEmptyInputs),
				key: 'addEmptyInputs'
			});
			this.workQueue.push ({
				task: lang.hitch (this, function () { this.performingCleanup = false; }),
				key: 'finishCleanup'
			});
		},

		_cleanupPlaceholders: function () {
			return asyncIterator (this.rows, function (/*Row*/row, /*Number*/rowIndex) {
				var i,
					rowWidth = 0;
				
				for (i = 0; i < row.blocks.length; ++ i) {
					rowWidth += row.blocks[i].get ('width');
				}
				
				var position = rowWidth;

				for (i = row.blocks.length - 1; i >= 0; -- i) {
					var block = row.blocks[i];
					position -= block.get ('width');
					
					if (!block || !block.isInstanceOf (Placeholder)) {
						continue;
					}
					
					var parent = this._getParentBlock (rowIndex, position),
						keep = false,
						p = parent,
						width = block.width;
					
					while (p) {
						var parentBlock = p.block,
							parentInputs = this._getBlockInputs (p.block, p.rowIndex, p.position);
						
						if (!parentBlock.isInstanceOf (Placeholder)) {
							if (parentInputs.length <= 1 || parentInputs.length <= parentBlock.minInputs) {
								keep = true;
							}
							break;
						}
						
						p = this._getParentBlock (p.rowIndex, p.position);
					}
					
					if (!keep) {
						console.log ('Removing placeholder');
						row._removeBlock (block, 0);
						
						p = parent;
						
						while (p) {
							if (p.block.width - width <= 0) {
								p.row._removeBlock (p.block, 0);
							} else {
								p.block.set ('width', p.block.width - width);
							}
							p = this._getParentBlock (p.rowIndex, p.position);
						}
					}

					console.log ('Cleanup placeholders: ', rowIndex, position);
				}
			}, this, 10);
		},
		
		_removeEmptyInputs: function () {
			return asyncIterator (this.rows, function (/*Row*/row) {
				array.forEach (row.blocks, function (block) {
					if (!block.isInstanceOf (Block) && !block.isInstanceOf (EmptyInput)) {
						return;
					}

					// Remove this block if it is an empty input with a placeholder parent:
					if (block.isInstanceOf (EmptyInput)) {
						
						var rowIndex = this._getRowIndex (block.row),
							offset = row._getBlockPosition (block),
							parent = this._getParentBlock (rowIndex, offset);
						
						console.log ('Attempting to remove empty input ', parent);
						
						if (!parent || !parent.block || !parent.block.isInstanceOf (Placeholder)) {
							return;
						}
						
						row._removeBlock (block, 1);
						return;
					}
					
					// Remove empty inputs above this block:
					var inputs = this._getBlockInputs (block),
						inputCount = array.filter (inputs, function (b) { return b.isInstanceOf (Block) || b.isInstanceOf (EmptyInput); }).length;

					// Remove empty inputs:
					if (inputCount > block.minInputs || block.isInstanceOf (EmptyInput)) {
						for (var i = 0; i < inputs.length; ++ i) {
							if (inputs[i].isInstanceOf (EmptyInput)) {
								inputs[i].row._removeBlock (inputs[i], 1);
							}
						}
						return;
					}
				}, this);
			}, this, 10);
		},
		
		_addEmptyInputs: function () {
			return asyncIterator (this.rows, function (/*Row*/row) {
				array.forEach (row.blocks, function (block) {
					if (!block.isInstanceOf (Block) && !block.isInstanceOf (EmptyInput)) {
						return;
					}
					
					var inputs = this._getBlockInputs (block),
						inputCount = array.filter (inputs, function (b) { return b.isInstanceOf (Block) || b.isInstanceOf (EmptyInput); }).length;

					if (inputCount >= block.minInputs) {
						return;
					}
					
					while (inputCount < block.minInputs) {
						var emptyInput = new EmptyInput ({
							width: 1
						});
						
						if (inputs.length > 0) {
							for (var i = 0; i < inputs.length; ++ i) {
								if (inputs[i].isInstanceOf (Placeholder)) {
									this._replaceBlock (inputs[i], emptyInput);
									break;
								}
							}
							if (i >= inputs.length) {
								this._addBlock (emptyInput, inputs[inputs.length - 1], 'right');
							}
							inputs = this._getBlockInputs (block);
						} else {
							this._addBlock (emptyInput, block, 'top');
						}
						
						++ inputCount;
					}
				}, this);
			}, this, 10);
		},
		
		_pushDown: function () {
			var rows = [ ],
				modified = false,
				deferred = new Deferred ();

			var cleanup = lang.hitch (this, function () {
				// Cleanup the tree again if one or more blocks have been pushed down:
				if (modified) {
					/*
					this._cleanup ().then (function () {
						deferred.resolve ();
					});
					*/
					deferred.resolve ();
				} else {
					deferred.resolve ();
				}
			});
			
			var processBlock = function (/*_BlockMixin*/block) {
				var rowIndex = this._getRowIndex (block.row),
					blockPosition = block.row._getBlockPosition (block),
					parentBlock = this._getParentBlock (rowIndex, blockPosition);
				
				if (!parentBlock || (block.isInstanceOf (EmptyInput) && !parentBlock.block.isInstanceOf (Placeholder)) || (!parentBlock.block.isInstanceOf (Placeholder) && !parentBlock.block.isInstanceOf (EmptyInput)) || parentBlock.block.width != block.width) {
					if (block.isInstanceOf (EmptyInput) && parentBlock && parentBlock.block.isInstanceOf (EmptyInput)) {
						block.row._replaceBlock (block, new Placeholder ({
							width: block.width,
							solid: false
						}));
					}
					return;
				}
				
				// Swap the two blocks:
				console.log ('Swapping blocks');
				inputs = this._getBlockInputs (block);
				if (inputs.length > 1) {
					var blockWidths = [ ];
					for (var i = 0; i < inputs.length; ++i) {
						blockWidths.push (inputs[i].width);
					}
					this._swapBlocks (block, parentBlock.block, blockWidths);
				} else {
					this._swapBlocks (block, parentBlock.block);
				}
				
				modified = true;
			};
			
			var processRow = function (/*Row*/row) {
				var blockFilter = function (/*Object*/block) {
					return block.isInstanceOf (Block) || block.isInstanceOf (EmptyInput);
				};
				
				// Shift blocks down in this row:
				array.forEach (array.filter (row.blocks, blockFilter), processBlock, this);
				
				// Remove empty rows:
				if (array.filter (row.blocks, blockFilter).length == 0) {
					this._removeRow (row);
				}
			};
	
			for (var i = 0; i < this.rows.length; ++ i) {
				rows.unshift (this.rows[i]);
			}
			
			asyncIterator (rows, processRow, this, 10).then (cleanup);
			
			return deferred;
		},
		
		_updateColumnWidth: function () {
			if (this.rows.length > 2) {
				return;
			}
			
			array.forEach (this.rows, function (row) {
				array.forEach (row.blocks, function (block) {
					if (block.get ('width') > 1) {
						block.set ('width', 1);
					}
				});
			});
		}
	});
});