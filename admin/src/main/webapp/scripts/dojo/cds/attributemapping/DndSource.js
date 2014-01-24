define ([
	'dojo/_base/declare',
	'dojo/dnd/Source'
], function (declare, Source) {
         
	var dndSourceAllowedClasses = {	};

	/**
	 * Custom DND source that overrides the _addItemClass method and only
	 * allows specific predefined classes to be set on DND items. This is
	 * used because the claro theme contains styles for dojo DND items that
	 * we don't want to be applied here.
	 */
	return declare ([Source], {
		_addItemClass: function (node, type) {
			if (type in dndSourceAllowedClasses) {
				this.inherited (arguments);
			}
		}
	});
});