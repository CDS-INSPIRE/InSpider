define ([
	'dojo/_base/declare'
], function (declare) {
	return declare ([], {
		constructor: function (/*Block*/block, /*Block[]*/inputs) {
			this.block = block;
			this.inputs = inputs;
		}
	});
});