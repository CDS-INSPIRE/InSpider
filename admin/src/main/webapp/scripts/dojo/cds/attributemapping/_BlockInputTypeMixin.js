define ([
	'dojo/_base/declare',
	'dojo/query',
	'dojo/dom-style',
	'dojo/dom-construct',
	'dojo/dom-class'
], function (declare, query, domStyle, domConstruct, domClass) {
	return declare ([], {
		expectedInputNode: null,
		expectedInputIconNode: null,
		expectedInputLabelNode: null,
		
		expectedInputType: null,
		expectedInputLabel: null,
		expectedInputDescription: null,
		
		_setExpectedInputTypeAttr: function (/*String*/expectedInputType) {
			var oldType = this.expectedInputType;
			
			this._set ('expectedInputType', expectedInputType);
			if (!this.expectedInputNode) {
				return;
			}
			
			if (oldType) {
				domClass.remove (this.expectedInputIconNode, 'type-' + oldType);
			}
			if (expectedInputType) {
				domClass.add (this.expectedInputIconNode, 'type-' + expectedInputType);
			}
			
			this._updateExpectedInputVisibility ();
		},
		
		_setExpectedInputLabelAttr: function (/*String*/expectedInputLabel) {
			this._set ('expectedInputLabel', expectedInputLabel);
			if (!this.expectedInputNode) {
				return;
			}
			
			domConstruct.empty (this.expectedInputLabelNode);
			domConstruct.place (document.createTextNode (expectedInputLabel), this.expectedInputLabelNode, 'last');
			
			this._updateExpectedInputVisibility ();
		},
		
		_setExpectedInputDescriptionAttr: function (/*String*/expectedInputDescription) {
			this._set ('expectedInputDescription', expectedInputDescription);
			if (!this.expectedInputNode) {
				return;
			}
		},
		
		_updateExpectedInputVisibility: function () {
			domStyle.set (this.expectedInputNode, 'display', this.expectedInputType && this.expectedInputLabel ? '' : 'none');
		},
		
		postCreate: function () {
			this.inherited (arguments);
			
			// Attempt to locate the input node:
			this.expectedInputNode = query ('.cdsAttributeMappingInputTypeInfo', this.domNode)[0];
			if (!this.expectedInputNode) {
				return;
			}
			
			var containerNode = domConstruct.create ('span', { 'class': 'container' }, this.expectedInputNode, 'last');
			this.expectedInputIconNode = domConstruct.create ('span', { 'class': 'icon' }, containerNode, 'last');
			this.expectedInputLabelNode = domConstruct.create ('span', { }, containerNode, 'last');
			
			if (this.expectedInputType) {
				this._setExpectedInputTypeAttr (this.expectedInputType);
			}
			if (this.expectedInputLabel) {
				this._setExpectedInputLabelAttr (this.expectedInputLabel);
			}
			if (this.expectedInputDescription) {
				this._setExpectedInputDescriptionAttr (this.expectedInputDescription);
			}
			
			this._updateExpectedInputVisibility ();
		}
	});
});