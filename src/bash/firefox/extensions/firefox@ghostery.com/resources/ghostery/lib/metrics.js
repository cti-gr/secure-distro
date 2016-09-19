/*!
 * Ghostery for Chrome
 * http://www.ghostery.com/
 *
 * Copyright 2014 Ghostery, Inc. All rights reserved.
 * See https://www.ghostery.com/eula for license.
 */

var MOBILE_MODE = (require('sdk/system').platform.toLowerCase() == 'android'),
	utils = require('./utils'),
	conf = require('./conf').load,
	Cc = require('chrome').Cc,
	Ci = require('chrome').Ci,
	SDK = {
		request: require('sdk/request')
	},
	xulRuntime = Cc["@mozilla.org/xre/app-info;1"].getService(Ci.nsIXULRuntime),
	OS = '';

{
	switch (xulRuntime.OS.toLowerCase()) {
		case 'darwin':
			OS = 'mac';
			break;
		case 'winnt':
			OS = 'win';
			break;
		case 'linux':
			OS = 'linux';
			break;
		case 'android':
			OS = 'android';
			break;
		default:
			OS = 'other';
	}
}

function sendReq(kind) {
	var metrics_url = 'https://d.ghostery.com/' + kind +
		'?gr=' + (conf.ghostrank ? '1' : '0') +
		'&v=' + encodeURIComponent(utils.VERSION) +
		'&os=' + encodeURIComponent(OS) +
		'&ua=' + (MOBILE_MODE ? 'fa' : 'ff');
	utils.log('XHR to ' + metrics_url);

	SDK.request.Request({
		url: metrics_url,
		overrideMimeType: 'image/gif'
	}).get();

	// set this even on upgrades to set flag when upgrading from < 5.4
	utils.prefs('install_recorded', true);
	utils.forceSave();
}

function recordInstall() {
	if (utils.prefs('install_recorded')) { return; }

	sendReq('install');
}

function recordUpgrade() {
	sendReq('upgrade');
}

exports.recordInstall = recordInstall;
exports.recordUpgrade = recordUpgrade;
