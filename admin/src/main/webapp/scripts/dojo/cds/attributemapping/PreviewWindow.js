define ([
	'dojo/_base/declare',
	'dojo/dom-class',
	'dojo/dom-style',
	'dojo/dom-construct',
	'dojo/query',
	'dojo/_base/array',
	'dojo/_base/lang',
	'put-selector/put',
	
	'dijit/Dialog',
	'dijit/layout/BorderContainer',
	'dijit/layout/ContentPane',
	'dijit/layout/AccordionContainer',
	'dijit/form/Button'
], function (declare, domClass, domStyle, domConstruct, query, array, lang, put, Dialog, BorderContainer, ContentPane, AccordionContainer, Button) {
	return declare ([BorderContainer], {
		width: '100px',
		height: '100px',
		
		headerContainer: null,
		featuresContainer: null,
		messagesContainer: null,
		refreshButton: null,
		
		messages: null,
		
		loading: true,
		
		postCreate: function () {
			this.inherited (arguments);
			
			domClass.add (this.domNode, 'cdsPreviewWindow');
			
			
			this.headerContainer = new ContentPane ({
				region: 'top',
				content: put ('div div.button + div.message <')
			});
			domClass.add (this.headerContainer.containerNode, 'cdsAttributePanelHeader');

			this.refreshButton = new Button ({
				label: 'Verversen',
				title: 'Features opnieuw ophalen en verwerken bij de server.',
				iconClass: 'icon-refresh',
				disabled: true,
				style: 'float: left;'
			}, query ('div.button', this.headerContainer.containerNode)[0]);
			
			this.loadingContainer = new ContentPane ({
				region: 'center',
				content: put ('div.loading h1 $', 'Bezig met laden van features ...')
			});
			
			this.featuresContainer = new ContentPane ({
				region: 'center',
				content: ''
			});
			
			this.messagesAccordion = new AccordionContainer ({
				region: 'bottom',
				splitter: true,
				style: 'height: 15em;',
				minSize: 50
			});
			this.messagesContainer = new ContentPane ({
				title: 'Foutmeldingen',
				content: 'Messages<br><br>Messages<br><br>messages<br><br>messages',
				style: 'width: 100%; height: 100%; overflow: auto;'
			});
			this.messagesAccordion.addChild (this.messagesContainer);
			
			this.addChild (this.headerContainer);
			this.addChild (this.loading ? this.loadingContainer : this.featuresContainer);
			if (!this.loading) {
				this.addChild (this.messagesAccordion);
			}
			
			// Event handlers:
			this.own (this.refreshButton.on ('click', lang.hitch (this, function (e) {
				this.emit ('previewmapping', {
					bubbles: true
				});
			})));
		},

		_setLoadingAttr: function (/*Boolean*/loading) {
			if (this.loading === loading) {
				return;
			}
			
			this._set ('loading', loading);
			
			if (!this.loadingContainer) {
				return;
			}
			
			if (loading) {
				this.removeChild (this.featuresContainer);
				this.addChild (this.loadingContainer);
				this.removeChild (this.messagesAccordion);
			} else {
				this.removeChild (this.loadingContainer);
				this.addChild (this.featuresContainer);
				this.addChild (this.messagesAccordion);
			}
			
			this.refreshButton.set ('disabled', loading);
		},
		
		_setMessagesAttr: function (messages) {
			this._set ('messages', messages);
			
			if (!this.messagesContainer) {
				return;
			}

			// Clear the messages panel:
			var containerNode = this.messagesContainer.containerNode;
			domConstruct.empty (containerNode);
			
			// Create a table:
			var tbody = put (containerNode, 'table thead tr th + th $ + th $ <<< tbody', 'Attribuut', 'Melding');
			
			var messageContainer = query ('div.message', this.headerContainer.containerNode)[0];
			dojo.empty (messageContainer);
			if (!messages || messages.length == 0) {
				put (messageContainer, 'div.success $', 'De features zijn succesvol gevalideerd na toepassen van de attribuutmapping.');
				this.removeChild (this.messagesAccordion);
				return;
			} else {
				put (messageContainer, 'div.failure $', 'Er zijn één of meerdere fouten opgetreden tijdens het verwerken van de features.');
			}
			
			this.addChild (this.messagesAccordion);
			
			array.forEach (messages, function (message) {
				var msg = message.message;
				if (message.attribute && msg.substr (0, message.attribute.length + 2) == message.attribute + ': ') {
					msg = msg.substr (message.attribute.length + 2);
				}
				
				put (
					tbody, 
					'tr td span[class=$] << td $ < td $', 
					'icon ' + (message.logLevel == 'ERROR' ? 'dijitIconError' : 'icon-warning'),
					message.attribute ? message.attribute : '-',
					msg
				);
			});
		},
		
		setFeatures: function (/*Array*/inputFeatures, /*Array*/outputFeatures, /*Object*/attributes) {
			if (!this.featuresContainer) {
				return;
			}
			
			var container = this.featuresContainer.containerNode;
			
			domConstruct.empty (container);
			
			var length = inputFeatures.length;
			
			if (length == 0) {
				put (container, 'h1 $', 'Er zijn geen features gevonden');
				return;
			}
			
			put (
					container, 
					'h1 $ < p $', 
					'Voorbeeldmapping voor ' + length + ' features.',
					'De eerste 10 features zijn opgehaald bij de WFS (linker kolom), hier zijn de filters en de attribuutmapping op toegepast en het resultaat van de mapping staat in de rechter kolom. Features die niet worden doorgelaten door de filters worden niet getoond in de rechter kolom.'
				);
			
			for (var i = 0; i < length; ++ i) {
				var inputFeature = inputFeatures[i],
					outputFeature = outputFeatures[i],
					row = put (container, 'div.feature em $ < table tbody tr.featureRow', 'Feature ' + (i + 1));
				
				var inputTableBody = put (row, 'td.featureCell.inputFeature table tbody');
				
				put (row, 'td.featureCell span.arrow');
					
				var outputTableBody = put (row, 'td.featureCell.outputFeature table tbody');
		
				var rows = [ ];
				for (var idx in inputFeature) {
					rows.push ({ label: idx, value: inputFeature[idx]});
				}
				rows = rows.sort (function (a, b) { return a.label < b.label ? -1 : (a.label > b.label ? 1 : 0); });
				array.forEach (rows, function (r) {
					put (inputTableBody, 'tr th $ + td $', r.label, r.value);
				});
				
				if (!outputFeature) {
					put (outputTableBody, 'tr td[colspan=2] $', 'Feature is weggefilterd, of er is geen mapping mogelijk (zie foutmeldingen).');
					continue;
				}
				
				rows = [ ];
				for (var idx in outputFeature) {
					rows.push ({ label: attributes[idx], value: outputFeature[idx] });
				}
				rows = rows.sort (function (a, b) { return a.label < b.label ? -1 : (a.label > b.label ? 1 : 0); });
				array.forEach (rows, function (r) {
					put (outputTableBody, 'tr th $ + td $', r.label, r.value);
				});
			}
		}
	});
});