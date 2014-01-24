define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/_base/array',
	'./asyncIterator',
	'./Block',
	'./_BlockInputTypeMixin',
	'dojo/Deferred',
	'./ConditionBlock'
], function (declare, lang, array, asyncIterator, Block, BlockInputTypeMixin, Deferred, ConditionBlock) {	
	return declare ([ ], {
		_doScheduleCleanupTasks: function () {
			this.inherited (arguments);
			
			// Cancel previously scheduled validation tasks, validation should always
			// be performed at the end of the work queue:
			this.workQueue.cancel ('updateBlockInputs');
			
			// Schedule the cleanup tasks:
			this.workQueue.push ({
				task: lang.hitch (this, this._updateBlockInputs),
				key: 'updateBlockInputs'
			});
		},
		
		_updateBlockInputs: function () {
			var rows = this.rows,
				deferred = new Deferred ();

			function processBlock (/*Block*/block) {
				var hasInputs = block.data && block.data.inputs;
				
				var inputBlocks = array.map (this._getBlockInputs (block), function (block) {
						if (block.isInstanceOf (BlockInputTypeMixin)) {
							return block;
						}
						
						return null;
					}),
					inputs = hasInputs ? block.data.inputs : [ ];
				
				for (var i = 0; i < inputBlocks.length; ++ i) {
					var input = inputs.length == 0 ? null : inputs[Math.min (inputs.length - 1, i)],
						inputBlock = inputBlocks[i];
					
					if (!inputBlock) {
						continue;
					}
					
					if (input && (i < inputs.length || input.variableInputCount) && hasInputs) {
						inputBlock.set ('expectedInputType', input.typeName);
						inputBlock.set ('expectedInputLabel', input.description);
						inputBlock.set ('expectedInputDescription', null);
					} else {
						inputBlock.set ('expectedInputType', null);
						inputBlock.set ('expectedInputLabel', null);
						inputBlock.set ('expectedInputDescription', null);
					}
				}
			}
			
			function processRow (/*Row*/row) {
				var blocks = row.blocks;
				array.forEach (
					array.filter (blocks, function (block) {
						return block.isInstanceOf (Block);
					}), 
					processBlock,
					this
				);
			}

			asyncIterator (rows, processRow, this).then (lang.hitch (this, function () {
				deferred.resolve ();
			}));
			
			return deferred;
		}
	});
});