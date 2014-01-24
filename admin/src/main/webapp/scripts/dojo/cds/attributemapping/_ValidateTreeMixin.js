define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/_base/array',
	'dojo/aspect',
	'dojo/Deferred',
	'dojox/html/entities',
	
	'./Block',
	'./asyncIterator'
], function (declare, lang, array, aspect, Deferred, htmlEntities, Block, asyncIterator) {
	
	return declare ([ ], {
		valid: false,
		
		_doScheduleCleanupTasks: function () {
			this.inherited (arguments);
			
			// Cancel previously scheduled validation tasks, validation should always
			// be performed at the end of the work queue:
			this.workQueue.cancel ('validate');
			
			// Schedule the cleanup tasks:
			this.workQueue.push ({
				task: lang.hitch (this, this._validate),
				key: 'validate'
			});
		},
		
		_validate: function () {
			var rows = this.rows,
				timeout = null,
				deferred = new Deferred (function () {
					clearTimeout (timeout);
				}),
				valid = true;
			
			var validator = {
				reject: function (/*Block*/block, /*String*/reason) {
					var errors = block.get ('errors');
					if (errors != '') {
						errors += '<br>';
					}
					errors += htmlEntities.encode (reason);
					block.set ('hasErrors', true);
					block.set ('errors', errors);
					valid = false;
				}
			};

			var processBlock = function (/*Block*/block) {
				block.set ({
					hasErrors: false,
					errors: ''
				});
				
				if (!block.validate) {
					block.set ('hasErrors', false);
					return;
				}
				
				var inputs = array.map (this._getBlockInputs (block), function (block) {
					if (block.isInstanceOf (Block)) {
						return block;
					}
					
					return null;
				});
				
				block.validate (block, inputs, validator);
			};
			
			var processRow = function (/*Row*/row) {
				var blocks = row.blocks;
				
				array.forEach (
					array.filter (blocks, function (block) {
						return block.isInstanceOf (Block);
					}), 
					processBlock,
					this
				);
			};
	
			asyncIterator (rows, processRow, this).then (lang.hitch (this, function () {
				this.set ('valid', valid);
				deferred.resolve ();
			}));
			
			return deferred;
		}
	});
});