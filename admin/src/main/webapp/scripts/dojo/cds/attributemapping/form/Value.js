define ([
	'dojo/_base/declare',
	'dojo/Stateful'
], function (declare, Stateful) {
	return declare ([Stateful], {
		value: null,
		
		constructor: function (value) {
			this.value = value;
		},
		
		postscript: function () {
		},
		
		getEditorWidget: function () {
			return null;
		},
		
		_labelGetter: function () {
			return this.get ('value');
		}
	});
});