define ([
	 'dojo/_base/declare',
	 './Block',
	 'dojo/dom-style',
	 'dojo/_base/lang',
	 'dojo/on'
], function (declare, Block, domStyle, lang, on) {
	return declare ([Block], {
		lastCondition: true,
		noTypes: true,
		typeDescription: 'Conditie',
		
		_setLastConditionAttr: function (/*Boolean*/lastCondition) {
			domStyle.set (this.addNode, 'display', lastCondition ? 'block' : 'none');
			this._set ('lastCondition', lastCondition);
		},
		
		_onClickDelete: function (e) {
			
			// Destroy this block in the next frame, after:
			if (this.tree) {
				this.tree.removeCondition (this);
			}
			
			e.preventDefault ();
		},
		
		_onClickAddCondition: function (e) {
			e.preventDefault ();
	
			if (this.tree) {
				console.log ('Emiting addCondition to: ', this._started);

				this.emit ('addcondition', { 
					bubbles: true,
					tree: this.tree
				});
			}
		}
	});
});