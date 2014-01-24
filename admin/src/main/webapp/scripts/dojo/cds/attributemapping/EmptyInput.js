define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'./_BlockMixin',
	'./_BlockInputTypeMixin',
	'./_TargetBlockMixin',
	'dijit/_TemplatedMixin',
	'dijit/_CssStateMixin',
	'dojo/dnd/Target',
	'dojo/text!./templates/AttributePanelEmptyInput.html'
], function (declare, lang, BlockMixin, BlockInputTypeMixin, TargetBlockMixin, TemplatedMixin, CssStateMixin, Target, emptyInputTemplate) {
	
	return declare ([BlockMixin, BlockInputTypeMixin, TargetBlockMixin, TemplatedMixin, CssStateMixin], {
		baseClass: 'cdsAttributePanelEmptyInput',
		
		solid: false,
		
		templateString: emptyInputTemplate,
		
		dndTargetNode: null,
		downArrowNode: null,
		
		postCreate: function () {
			this.inherited (arguments);
			
			this.dndTargets = [
				this.createTarget (this.dndTargetNode),
				this.createTarget (this.downArrowNode)
			];
		},
		
		uninitialize: function () {
			this.inherited (arguments);
		},
		
		_dropBlock: function (/*Block*/block) {
			console.log ('Dropping block on empty input: ', block, block.title);
			if (this.tree) {
				this.tree.replaceBlock (this, block);
			}
		}
	});
	
});