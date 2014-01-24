define ([
	'dojo/_base/declare',
	'dijit/_WidgetBase',
	'dijit/_TemplatedMixin',
	'dojo/dom-class',
	
	'./Block',
	
	'dojo/text!./templates/Operation.html'
], function (declare, WidgetBase, TemplatedMixin, domClass, Block, operationTemplate) {
	return declare ([WidgetBase, TemplatedMixin], {
		baseClass: 'cdsOperation',
		templateString: operationTemplate,
		
		titleNode: null,
		typeNode: null,
		
		title: '',
		_setTitleAttr: { node: 'titleNode', type: 'innerText' },
		
		operationDescription: '',
		
		type: 'default',
		_setTypeAttr: function (type) {
			if (this.type) {
				domClass.remove (this.typeNode, this.baseClass + '-' + this.type);
			}
			domClass.add (this.typeNode, this.baseClass + '-' + type);
			this._set ('type', type);
		},
		
		typeDescription: '',
		
		_setIsInputAttr: function (/*Boolean*/isInput) {
			this._set ('isInput', isInput);
			domClass[isInput ? 'add' : 'remove'] (this.domNode, this.baseClass + 'Input');
		},
		
		blockFactory: function () {
			console.log (Block);
			return new Block ({
				title: 'Inserted block',
				width: 1,
				solid: true,
				noInputs: true,
				canEdit: false
			});
		}
	});
});