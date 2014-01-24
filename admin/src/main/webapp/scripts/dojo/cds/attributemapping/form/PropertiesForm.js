define ([
    'dojo/_base/declare',
    'dojo/_base/array',
    'dojo/_base/lang',
	'dijit/_WidgetBase',
	'dojo/dom-construct',
	'dojo/dom-class',
	'dojo/Stateful',
	
	'dijit/form/TextBox',
	'dijit/form/CheckBox',
	'dijit/form/NumberSpinner',
	'dijit/form/Button'
], function (declare, array, lang, WidgetBase, domConstruct, domClass, Stateful, TextBox, CheckBox, NumberSpinner, Button) {
	
	var typeMap = {
		'string': TextBox,
		'object': TextBox,
		'boolean': CheckBox,
		'number': NumberSpinner
	};
	
	
	return declare ([WidgetBase], {
		baseClass: 'cdsAttributePanelPropertiesForm',
		
		values: null,
		childWidgets: null,
		watchHandles: null,
		eventHandles: null,
		
		rowsNode: null,
		buttonsNode: null,
		closeButton: null,
		
		buildRendering: function () {
			this.inherited (arguments);
			
			console.log ('Buildrendering');
			
			domClass.add (this.domNode, this.baseClass);
			
			this.rowsNode = domConstruct.create ('div', {
				'class': this.baseClass + 'Rows'
			}, this.domNode, 'last');
			
			this.buttonsNode = domConstruct.create ('div', {
				'class': this.baseClass + 'Buttons'
			}, this.domNode, 'last');
			
			this.closeButton = new Button ({
				label: 'Sluiten'
			}, domConstruct.create ('span', { }, this.buttonsNode, 'last'));
			
			this.closeButton.on ('Click', lang.hitch (this, function () {
				this.onClose (); 
			}));
		},
		
		startup: function () {
			this.inherited (arguments);
			
			this._setValuesAttr (this.values);
		},
		
		destroyDescendants: function () {
			this.inherited (arguments);
			
			this._destroyForm ();
			
			if (this.buttonsNode) {
				this.buttonsNode.destroyRecursive ();
				this.buttonsNode = null;
			}
		},

		onClose: function () {
		},
		
		setEnabled: function (/*String*/name, /*Boolean*/state) {
			if (!this.widgetMap || !this.widgetMap[name]) {
				return;
			}
			
			this.widgetMap[name].set ('disabled', !state);
		},
		
		_setValuesAttr: function (/*Stateful*/values) {
			console.log ('_setValuesAttr: ', values, this._started);
			if (values != null && (!values.isInstanceOf || !values.isInstanceOf (Stateful))) {
				throw new Error ('Only instances of dojo/Stateful can be used as the model of a PropertiesForm');
			}
			
			// Set the new value:
			var oldValues = this.values;
			this._set ('values', values);
			
			console.log (this._started, this._builtOnce, values != oldValues);
			if (this._started && (!this._builtOnce || values != oldValues)) {
				console.log ('Building form');
				this._builtOnce = true;
				this._buildForm (values);
			}
		},
		
		_buildForm: function (/*Stateful*/values) {
			console.log ('_buildForm ', values);
			this._destroyForm ();
			
			// Leave the form blank in case there are no values:
			if (!values) {
				return;
			}
			
			this.childWidgets = [ ];
			this.watchHandles = [ ];
			this.eventHandles = [ ];
			this.widgetMap = { };
			
			for (var i in values) {
				if (!values.hasOwnProperty (i) || i.charAt (0) == '_') {
					continue;
				}
				
				this._buildItem (values, i);
			}
			
			// Trigger a modified event after constructing the form:
			if (values._onModified) {
				values._onModified (this);
			}
		},
		
		_buildItem: function (/*Stateful*/stateful, /*String*/name) {
			console.log ('_buildItem ', name, this.rowsNode);
			var value = stateful.get (name),
				type = typeof value;
			
			if (!typeMap[type]) {
				console.log ('Unknown type: ', name, type);
				return;
			}
			
			var	container = domConstruct.create ('div', { 'class': this.baseClass + 'Row' }),
				label = domConstruct.create ('label', { 'class': this.baseClass + 'Label' }, container, 'last'),
				inputContainer = domConstruct.create ('span', { 'class': this.baseClass + 'Input' }, container, 'last'),
				control,
				bindProperty,
				description = stateful._descriptions && name in stateful._descriptions ? stateful._descriptions[name] : name;
			
			if (value && value.getEditorWidget) {
				control = value.getEditorWidget (lang.hitch (this, function () {
					this._triggerModified ();
				}));
				control.placeAt (inputContainer, 'last');
			} else {
				var input = domConstruct.create ('span', { }, inputContainer, 'last');
				
				if (type == 'boolean') {
					control = new CheckBox ({
						checked: value
					}, input);
					bindProperty = 'checked';
				} else {
					control = new typeMap[type] ({
						value: value 
					}, input);
					bindProperty = 'value';
				}
				
				// Bind the property
				this.watchHandles.push (this._bind (stateful, name, control, bindProperty));
				this.watchHandles.push (this._bind (control, bindProperty, stateful, name, true));
			}

			this.childWidgets.push (control);
			this.widgetMap[name] = control;
			
			domConstruct.place (document.createTextNode (description), label, 'last');
			domConstruct.place (container, this.rowsNode, 'last');
		},
		
		_destroyForm: function () {
			if (this.childWidgets) {
				array.forEach (this.childWidgets, function (widget) {
					widget.destroyRecursive ();
				});
				this.childWidgets = null;
			}
			
			if (this.watchHandles) {
				array.forEach (this.watchHandles, function (h) {
					h.remove ();
				});
				this.watchHandles = null;
			}
			
			if (this.eventHandles) {
				array.forEach (this.eventHandles, function (h) {
					h.remove ();
				});
				this.eventHandles = null;
			}
			
			if (this.rowsNode) {
				domConstruct.empty (this.rowsNode);
			}
		},
		
		_bind: function (/*Stateful*/sourceStateful, /*String*/sourceProperty, /*Stateful*/destinationStateful, /*String*/destinationProperty, /*Boolean?*/triggerModified) {
			return sourceStateful.watch (sourceProperty, lang.hitch (this, function (property, oldValue, newValue) {
				console.log (oldValue, ' -> ', newValue);
				destinationStateful.set (destinationProperty, newValue);
				if (triggerModified) {
					this._triggerModified ();
				}
			}));
		},
		
		_triggerModified: function () {
			this.emit ('modified', {
				bubbles: false
			});
			
			if (this.values && this.values._onModified) {
				this.values._onModified (this);
			}
		}
	});
});