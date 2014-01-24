define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'dojo/_base/array',
	'dojo/when',
	'dojo/Deferred'
], function (declare, lang, array, when, Deferred) {
	return declare ([ ], {
		synchronousTaskCount: 4,
		queue: null,
		working: false,
		currentTask: null,
		
		constructor: function () {
			this.queue = [ ];
			this.working = false;
		},
		
		push: function (/*Function|Object|Array*/tasks) {
			if (!lang.isArray (tasks)) {
				tasks = [ tasks ];
			}
			
			for (var i = 0; i < tasks.length; ++ i) {
				var t = tasks[i];
				
				if (t.task) {
					this._pushTask (t.task, t.key);
				} else {
					this._pushTask (t);
				}
			}
			
			this._execute ();
		},
		
		cancel: function (/*Object*/key) {
			var removedItems = [ ];
			this.queue = array.filter (this.queue, function (task) {
				if (task.key === key) {
					removedItems.push (task);
					return false;
				}
				return true;
			});
			return removedItems;
		},
		
		flush: function () {
			this.queue = [ ];
		},
		
		mark: function () {
			var d = new Deferred ();
			this.push (function () {
				d.resolve ();
			});
			return d;
		},
		
		_pushTask: function (/*Function*/task, /*Object?*/key) {
			this.queue.push ({
				task: task,
				key: key
			});
		},
		
		_execute: function () {
			if (this.working || this.queue.length <= 0) {
				return;
			}
			
			this.working = true;
			
			setTimeout (lang.hitch (this, this._doExecute), 0);
		},
		
		_doExecute: function () {
			if (this.queue.length == 0) {
				this.working = false;
				return;
			}

			var count = 0;
			
			while (this.queue.length > 0 && count < this.synchronousTaskCount) {
				var task = this.queue.shift ();
	
				this.currentTask = task;

				var result = task.task (this);
				
				if (result && result.then) {
					result.then (lang.hitch (this, function () {
						this.currentTask = null;
						this._doExecute ();
					}), lang.hitch (this, function () {
						this.currentTask = null;
						this._doExecute ();
					}));
					return;
				}
				
				++ count;
			}
			
			this.currentTask = null;
			setTimeout (lang.hitch (this, this._doExecute), 0);
		}
	});
});