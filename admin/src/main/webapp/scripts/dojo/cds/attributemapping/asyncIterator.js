define ([
	'dojo/_base/declare',
	'dojo/Deferred',
	'dojo/when'
], function (declare, Deferred, when) {
	return function (/*List|Promise*/list, /*Function*/callback, /*Object*/scope, /*Number*/batchSize) {
		if (typeof batchSize == "undefined" || batchSize < 1) {
			batchSize = 1;
		}
		
		var cancelled = false,
			deferred = new Deferred (function (reason) {
				cancelled = true;
			});
		
		when (list, function () {
			if (cancelled) {
				return;
			}
			
			var index = 0;
			
			function finish () {
				if (cancelled) {
					deferred.reject ('cancelled');
				} else {
					deferred.resolve (list);
				}
			}
			
			function processItem () {
				if (index >= list.length || cancelled) {
					return;
				}
				
				var item = list[index],
					result = callback.apply (scope || list, [item, index, list]);
				
				++ index;

				return result;
			}
			
			function processBatch () {
				for (var i = 0; i < batchSize; ++ i) {
					if (index >= list.length || cancelled) {
						finish ();
						return;
					}
					
					var result = processItem ();
					
					if (result && result.then) {
						when (result, function (value) {
							setTimeout (processBatch, 0);
						}, function (reason) {
							deferred.reject (reason);
						});
					}
				}
				
				setTimeout (processBatch, 0);
			}
			
			setTimeout (processBatch, 0);
		}, function (reason) {
			deferred.reject (reason);
		});
		
		return deferred;
	};
});