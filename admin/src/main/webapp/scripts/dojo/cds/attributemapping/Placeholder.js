define ([
	'dojo/_base/declare',
	'./_BlockMixin',
	'dijit/_TemplatedMixin',
	'dijit/_CssStateMixin',
	'dojo/text!./templates/AttributePanelPlaceholder.html'
], function (declare, BlockMixin, TemplatedMixin, CssStateMixin, placeholderTemplate) {
	
	return declare ([BlockMixin, TemplatedMixin, CssStateMixin], {
		baseClass: 'cdsAttributePanelPlaceholder',
		
		solid: false,
		
		templateString: placeholderTemplate
	});
});