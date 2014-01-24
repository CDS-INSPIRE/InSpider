define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/_base/array',
	'dojo/Deferred',
	'./Block',
	'./Placeholder',
	'./EmptyInput',
	'./asyncIterator'
], function (declare, lang, array, Deferred, Block, Placeholder, EmptyInput, asyncIterator) {

	return declare ([ ], {
		
		_doScheduleCleanupTasks: function () {
			this.inherited (arguments);
			
			this.workQueue.push ({
				task: lang.hitch (this, this._cleanup),
				key: 'cleanup'
			});
			this.workQueue.push ({
				task: lang.hitch (this, this._pushDown),
				key: 'pushDown'
			});
		},
		
		_cleanup: function () {
			var deferred = new Deferred ();
			
			// for each row
			//   for each block
			//     if the parent block is wider than this block and this block is not solid then,
			//       remove this (placeholder) block
			//       for each parent
			//         decrease parent width by one
			for (var i = 0; i < this.rows.length; ++ i) {
				var row = this.rows[i],
					blocks = row.blocks,
					position = 0;
				
				for (var j = 0; j < blocks.length; ++ j) {
					var block = blocks[j],
						width = block.width,
						parentBlock = this._getParentBlock (i, position),
						removed = false;
	
					// Update the solid flag of the parent block:
					if (block.solid && parentBlock && !parentBlock.block.solid) {
						parentBlock.block.set ('solid', true);
					} else if (!block.solid && parentBlock && parentBlock.block.width == width && parentBlock.block.solid && parentBlock.block.isInstanceOf (Placeholder)) {
						parentBlock.block.set ('solid', false);
					}
	
					console.log ('Candidate block for removal: ', block.expectedInputLabel, parentBlock, block.solid);
					
					// Remove this block when possible or replace by an empty input:
					if (!block.solid 
							&& parentBlock 
							&& (width < parentBlock.block.width || (parentBlock.block.width == 1 && !parentBlock.block.solid))) {
						
						console.log ('Removing, min inputs: ', parentBlock.block.minInputs, parentBlock.block.title);
						
						// Keep blocks if one of the parents would have too few inputs after removing this block:
						var	p = parentBlock,
							keep = false;
						
						while (p) {
							var inp = this._getBlockInputs (p.block, p.rowIndex, p.position);
							if (p.block.minInputs > 0 && inp.length <= p.block.minInputs) {
								keep = true;
							} 
							
							if (p && p.block.isInstanceOf (Block)) {
								if (!keep) {
									keep = inp.length <= 1;
								}
								break;
							}
							
							p = this._getParentBlock (p.rowIndex, p.position);
						}
						
						// Remove the block or replace by a placeholder:
						console.log (parentBlock.block.minInputs, this._getBlockInputs (parentBlock.block, parentBlock.rowIndex, parentBlock.position));
						if (parentBlock && parentBlock.block.minInputs > 0 
								&& this._getBlockInputs (parentBlock.block, parentBlock.rowIndex, parentBlock.position).length <= parentBlock.block.minInputs) {
						
							if (!block.isInstanceOf (EmptyInput)) {
								row._replaceBlock (block, new EmptyInput ({
									tree: this,
									row: row,
									solid: false,
									width: width
								}));
							}
						} else if (!keep) {
							removed = true;
							row._removeBlock (block, 0);
							
							while (parentBlock) {
								if (parentBlock.block.width - width <= 0) {
									parentBlock.row._removeBlock (parentBlock.block, 0);
								} else {
									parentBlock.block.set ('width', parentBlock.block.width - width);
								}
								parentBlock = this._getParentBlock (parentBlock.rowIndex, parentBlock.position);
							}
						}
						
					}  
					
					if (!removed) {
						position += width;
					}
				}
			}
			
			deferred.resolve ();
			
			return deferred;
		},
		
		_pushDown: function () {
			var rows = [ ],
				modified = false,
				deferred = new Deferred ();

			var cleanup = lang.hitch (this, function () {
				// Cleanup the tree again if one or more blocks have been pushed down:
				if (modified) {
					this._cleanup ().then (function () {
						deferred.resolve ();
					});
				} else {
					deferred.resolve ();
				}
			});
			
			var processBlock = function (/*_BlockMixin*/block) {
				var rowIndex = this._getRowIndex (block.row),
					blockPosition = block.row._getBlockPosition (block),
					parentBlock = this._getParentBlock (rowIndex, blockPosition);
				
				if (!parentBlock || block.isInstanceOf (EmptyInput) || (!parentBlock.block.isInstanceOf (Placeholder) && !parentBlock.block.isInstanceOf (EmptyInput)) || parentBlock.block.width != block.width) {
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
			
			asyncIterator (rows, processRow, this).then (cleanup);
			
			return deferred;
		}		
	});
});