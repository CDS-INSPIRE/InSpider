define ([
	'dojo/_base/declare',
	'./Row',
	'dojo/dom-class'
], function (declare, Row, domClass) {
	return declare ([Row], {
		postCreate: function () {
			this.inherited (arguments);
			
			domClass.add (this.domNode, 'cdsAttributePanelConditionRow');
		}
	});
});