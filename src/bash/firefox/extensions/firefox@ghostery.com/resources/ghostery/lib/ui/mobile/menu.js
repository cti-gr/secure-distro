/*!
 * Ghostery for Firefox
 * http://www.ghostery.com/
 *
 * Copyright 2014 Ghostery, Inc. All rights reserved.
 * See https://www.ghostery.com/eula for license.
 */

var SDK = {
		windows: require('sdk/window/utils'),
		self: require('sdk/self')
	},
	t = require('ghostery/i18n').t,
	MENU_ID = null;

function add(onCommandCallback) {
	var window = SDK.windows.getMostRecentBrowserWindow();
	// TODO FF MOBILE: find a way to not use DATA uri, use file:// instead.
	MENU_ID = window.NativeWindow.menu.add(t('options_page_title'), 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA3NpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNS1jMDIxIDc5LjE1NDkxMSwgMjAxMy8xMC8yOS0xMTo0NzoxNiAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo1NGE1MWUwNC00OGZjLTQ2ZjctOTgxZi1hMGQ2Y2I3OGVhM2QiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6NThEM0Y0MkRCMzlFMTFFMzg5MzZEN0IyODcwNzc2RDMiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6NThEM0Y0MkNCMzlFMTFFMzg5MzZEN0IyODcwNzc2RDMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIChNYWNpbnRvc2gpIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6N2Y3YzNiNjItNDdhZC00YzIxLWFlMGItYjliNDZjZjU1YzFiIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjU0YTUxZTA0LTQ4ZmMtNDZmNy05ODFmLWEwZDZjYjc4ZWEzZCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Ptja1FEAAALwSURBVHjaxFdLTxNRFD4zfdhWW4qS0qIooIISFy4wGohRC24EEjUmEDSIbkxc+AdMjDEuTFz4StSdiZFgXBmDKwPBhW58RENiNKIW6gM1QEuL2LffnUzLZNphZlpGTvLlnp47c+83556eew6XyWRoOYWnZRaz3MBxnOLD/sHgKgxHgcNAE7BanAoCz4H7wKPhjuq00hpyj3N5BgUC2LwHwxXAo/JRo8BJkHi5JASwMTNcBc7o8Gwc6AOJATUCWmLgos7NmViBeyB/QO3BRT2ABfwYhkqIsWmgEZ74qdsDouuvye0uKy9ALitMHFXYTHIzC9Lzxf4N24BtUkOdy0L9+3yAV9Cz4rTwdGePlwb8Pmrx2uXr9OFjyoohcERu6K5zksPMATx1Qc9Kx/qVVGk3EQ+fHd/skr9mAzqLIdAsNzS4rTl9i0SvL1vQN8IzFp5TXUsLgfq8hyXrcgr2Qr8hm4ohYJUbPoYTOX1strA+EU1QLJVRXauou+DB5wgl0hkBTM/K4MQcheJpYtv2j0VKuwsWk/ehOPWOTAr6r/lUzj4TS9GJp5NUbjXReDRhHAFOtrFUZuEBBpbH9Nzwuo5gt88uJBxFgphqrXIYVw8Eo0m63uyhRnd+TFU5zHRpRwUl0xnjjuBLJEGj0zG60eKhAHQW/SwgNzgttBW54C3mRn7MG0eAye13YfIg57OUW+NcSMefQObC66nSKyLxIrIpvZBEhJ17NUX71zpoL84b1wC9+B2jh4Go4A0Fcen1wBo15k++/RGgUWr1BmHDEteebni1Rg+BnQYUwH49BNoMINCpqSRrffy1EsN3A3oGVqh6h9rXzah5oNughoVlry4tR9BjYCN0TMvfsEkcWVYZBgJAO6tudW72BrgJsOhn5fl2YJcWAr1iq/UM5XRKTEyXMXwAyiXPhcUNpsW+oVoy9xc4hPcD4u+zWKO20Efoac0OsmYDmANusW4JG4TEOXa+p0QiPuA05u4W1Zr9b/knwABcTPVApFGP4wAAAABJRU5ErkJggg==', onCommandCallback);
}
exports.add = add;

function remove() {
	if (MENU_ID !== null) {
		var window = SDK.windows.getMostRecentBrowserWindow();
		MENU_ID = window.NativeWindow.menu.remove(MENU_ID);
	}
}
exports.remove = remove;