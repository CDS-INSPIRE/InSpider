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
		_doScheduleCleanupTasks: function () {
			this.inherited (arguments);
			
			this.workQueue.push ({
				task: lang.hitch (this, this._addEmptyInputs),
				key: 'emptyInputs'
			});
		},
		
		_addEmptyInputs: function () {
			return asyncIterator (this.rows, function (/*Row*/row) {
				array.forEach (row.blocks, function (block) {
					if (!block.isInstanceOf (Block) && !block.isInstanceOf (EmptyInput)) {
						return;
					}
					
					var inputs = this._getBlockInputs (block),
						inputCount = array.filter (inputs, function (b) { return b.isInstanceOf (Block) || b.isInstanceOf (EmptyInput); }).length;

					// Remove empty inputs:
					if (inputCount > block.minInputs || block.isInstanceOf (EmptyInput)) {
						for (var i = 0; i < inputs.length; ++ i) {
							if (inputs[i].isInstanceOf (EmptyInput)) {
								this.removeBlock (inputs[i]);
								return;
							}
						}
						return;
					} else if (inputCount >= block.minInputs) {
						return;
					}
					
					var emptyInput = new EmptyInput ({
						width: 1
					});
					
					if (inputs.length > 0) {
						for (var i = 0; i < inputs.length; ++ i) {
							if (inputs[i].isInstanceOf (Placeholder)) {
								this.replaceBlock (inputs[i], emptyInput);
								return;
							}
						}
						this.addBlock (emptyInput, inputs[inputs.length - 1], 'right');
					} else {
						this.addBlock (emptyInput, block, 'top');
					}
				}, this);
			}, this);
		}
	});
});