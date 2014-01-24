var profile = (function () {
	return {
		basePath: '.',
		releaseDir: '../js-release/scripts',
		releaseName: 'cds',
		action: 'release',
		
		layerOptimize: 'shrinksafe',
		optimize: 'shrinksafe',
		cssOptimize: 'comments',
		mini: true,
		stripConsole: 'all',
		selectorEngine: 'lite',
		
		// Needed for backwards compatibility (e.g. invoking parser as dojo.parser):
		insertAbsMids: 1,
		
		staticHasFeatures: {
			'dojo-guarantee-console': 0,
			'dojo-loader': 0,
			'dojo-trace-api': 0,
			'dojo-log-api': 0,
			'dojo-publish-privates': 0,
			'dojo-sync-loader': 0,
			'dojo-test-sniff': 0
		},
		
		packages: [{
			name: 'dojo',
			location: 'dojo'
		}, {
			name: 'dijit',
			location: 'dijit'
		}, {
			name: 'dojox',
			location: 'dojox'
		}, {
			name: 'put-selector',
			location: 'put-selector'
		}, {
			name: 'cds',
			location: 'cds'
		}],
		
		layers: {
			'dojo/dojo': {
				include : [
				           'dojo/dojo', 
				           'dojo/parser', 
				           'dojo/_base/fx', 
				           'dojo/query', 
				           'dojo/domReady', 
				           'dojox/widget/DialogSimple', 
				           'dijit/layout/ContentPane',
				           'dijit/form/Button'
				           ],
				customBase: true,
				boot: true
			},
			
			'cds/layer-datasetconfig': {
				include: ['dijit/form/FilteringSelect',
				          'dijit/form/CheckBox', 'dijit/form/Button'
				          ]
			},
			
			'cds/layer-etloverzicht': {
				include: ['dijit/form/Form', 'dijit/form/FilteringSelect', 'dojo/date/stamp', 'dojo/date/locale', 'dojo/store/JsonRest',
				          'dojo/store/Memory', 'dojo/data/ObjectStore', 'dojox/grid/DataGrid', 'dojox/layout/TableContainer', 'dijit/form/Button']
			},
			
			'cds/layer-monitoring': {
				include: ['dojo/store/JsonRest', 'dojo/date/locale', 'dojo/store/Memory', 'dojo/data/ObjectStore',
				          'dojox/grid/DataGrid', 'dijit/Menu', 'dijit/MenuItem', 'dijit/form/Button']
			},
			
			'cds/layer-naw': {
				include: ['dijit/form/FilteringSelect', 'dijit/form/ValidationTextBox', 'dijit/form/Button']
			},
			
			'cds/layer-validation': {
				include: ['dijit/form/FilteringSelect', 'dijit/form/ComboBox', 'dijit/form/ValidationTextBox', 'dojo/date/stamp',
				          'dojo/date/locale', 'dojo/store/JsonRest', 'dojo/store/Memory', 'dojo/data/ObjectStore', 
				          'dojo/data/ItemFileWriteStore', 'dojo/data/ItemFileReadStore']
			},
			
			'cds/layer-vdconfig': {
				include: ['dijit/form/Form', 'dijit/form/FilteringSelect', 'dijit/layout/TabContainer', 'dijit/layout/ContentPane', 'dijit/form/Textarea',
				          'dojox/form/ListInput', 'dijit/form/Button', 'dijit/form/TextBox']
			},
			
			'cds/layer-gebruikersbeheer-gebruiker': {
				include: ['dijit/form/Form', 'dijit/form/CheckBox', 'dijit/form/FilteringSelect', 'dijit/form/Button', 'dijit/form/TextBox']
			},
			
			'cds/layer-gebruikersbeheer-gebruikers': {
				include: ['dijit/form/Form', 'dijit/form/FilteringSelect', 'dijit/form/CheckBox', 'dojo/store/JsonRest',
				          'dojo/store/Memory', 'dojo/data/ObjectStore', 'dojox/grid/DataGrid', 'dijit/form/Button', 'dijit/form/TextBox']
			},
			
			'cds/layer-attributemapping-form': {
				include: ['cds/attributemapping/MappingController', 'cds/attributemapping/MappingPanel', 'dojo/NodeList-html', 'dijit/registry']
			}
		}
	};
}) ();