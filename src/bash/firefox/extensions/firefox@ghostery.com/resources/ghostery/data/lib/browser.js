/*!
 * Ghostery for Firefox
 * http://www.ghostery.com/
 *
 * Copyright 2014 Ghostery, Inc. All rights reserved.
 * See https://www.ghostery.com/eula for license.
 */

/* global _, Backbone, getTemplate, t_blocking_summary, i18n, Ghostery */
/* jshint unused: false */

var block_default_shown = false;

var TAGS;

function t_blocking_summary(num_selected, num_total, type, small) {
	if (num_selected === 0) {
		if (small) {
			return i18n.t('blocking_summary_none_small_firefox');
		} else {
			return i18n.t('blocking_summary_' + type + '_none_firefox');
		}
	} else if (num_selected === num_total) {
		if (small) {
			return i18n.t('blocking_summary_all_small_firefox');
		} else {
			return i18n.t('blocking_summary_' + type + '_all_firefox');
		}
	} else {
		if (small) {
			return i18n.t('blocking_summary_small_firefox', num_selected, num_total);
		} else {
			return i18n.t('blocking_summary_' + type + '_firefox', num_selected, num_total);
		}
	}
}

var dispatcher = _.clone(Backbone.Events);

var AppInfo = Backbone.Model.extend({
	defaults: {
		hidden: true,
		loading: true,
		success: false
	},
	sync: function (method, model, opts) {
		
		// TODO FF: verify this is the correct way of implementing commented code
		self.port.emit('ajaxRequest', {
			url: 'https://www.ghostery.com/apps/' + encodeURIComponent(model.get('name').replace(/\s+/g, '_').toLowerCase()) + '?format=json'
		});
		self.port.on('ajaxResponse', function (data) {
			if (data.success) {
				data = data.json;
				data.success = true;
				data.loading = false;
				data.company_in_their_own_words = data.company_in_their_own_words || "";
				data.tags = data.tags || [];
				opts.success(data);
			} else {
				model.set('loading', false);
			}
		});
	}
});

var AppInfoView = Backbone.View.extend({
	template: getTemplate('_app_info'),

	initialize: function () {
		this.model.on('change:hidden', this.toggleContents, this);

		this.model.on('change:loading', function () {
			if (this.model.get('hidden')) {
				this.render();
			} else {
				this.model.set('hidden', true, { silent: true });
				this.render();
				this.model.set('hidden', false);
			}
		}, this);

		this.model.fetch();
	},

	render: function () {
		this.$el.html(this.template(this.model.toJSON()));
		return this;
	},

	toggleContents: function () {
		var $el = this.$el;

		if (this.model.get('hidden')) {
			this.$el.find('div').slideUp(null, function () {
				$el.hide();
			});
		} else {
			this.$el.show().find('div').slideDown(null, function () {
				$el.scrollIntoGreatness();
			});
		}
	}
});

var App = Backbone.Model.extend({
	defaults: {
		hidden: false
	}
});

var AppView = Backbone.View.extend({
	className: 'app',
	template: getTemplate('_app'),

	initialize: function (opts) {
		this.model.on('change:hidden', this.toggleVisibility, this);
		this.model.on('change:selected', this.updateCheckbox, this);

		this.model.set('disabled', opts.disabled);
	},

	events: {
		'click .app-checkbox': function (e) {
			this.model.set('selected', e.target.checked);
		},

		'click a': function (e) {
			e.preventDefault();
			if (this.model.get('userCreated')) { return; }

			if (!this.appInfo) {
				// create the app info model
				this.appInfo = new AppInfo({
					name: this.model.get('name')
				});

				// render it
				this.$el.after((new AppInfoView({
					model: this.appInfo
				})).render().el);

				// listen to app info open events
				window.dispatcher.on('app:info:open', function (app_id) {
					if (app_id != this.model.get('id')) {
						// close our app info
						this.appInfo.set('hidden', true);
					}
				}, this);
			}

			this.toggleAppInfo();
		},

		'click .app-tags a': function (e) {
			e.preventDefault();
			e.stopPropagation();
			var tag_id = $(e.target).data('id');

			this.model.get('tags').get(tag_id).set('selected', true);
		}

	},

	render: function () {
		// TODO better idiom for toggling this.$el's visiblity?
		this.$el.toggleClass('hidden', this.model.get('hidden'));

		this.el.innerHTML = this.template(this.model.toJSON());

		return this;
	},

	toggleAppInfo: function () {
		this.appInfo.set('hidden', !this.appInfo.get('hidden'));

		if (!this.appInfo.get('hidden')) {
			window.dispatcher.trigger('app:info:open', this.model.get('id'));
		}
	},

	updateCheckbox: function () {
		this.$el.find('input[type=checkbox]').prop('checked', this.model.get('selected'));
	},

	toggleVisibility: function () {
		// TODO better idiom for toggling this.$el's visiblity?
		this.$el.toggleClass('hidden', this.model.get('hidden'));

		if (this.appInfo) {
			if (this.model.get('hidden')) {
				this.appInfo.set('hidden', true);
			}
		}
	}
});

var Apps = Backbone.Collection.extend({
	model: App,
	comparator: function (app) {
		return app.get('name').toLowerCase();
	}
});

var Tag = Backbone.Model.extend({
	defaults: {
		selected: false,
		count: 0
	},
	incrementCount: function () {
		this.set('count', this.get('count') + 1);
	}
});

var TagView = Backbone.View.extend({
	className: 'tag',
	template: getTemplate('_tag'),

	initialize: function () {
		this.model.on('change:selected', this.render, this);
	},

	events: {
		'click .tag-button': function () {
			this.model.set('selected', !this.model.get('selected'));
			this.render();
		}
	},

	render: function () {
		this.$el.html(this.template(this.model.toJSON()));
		if (!Ghostery.MOBILE_MODE) {
			this.$('.tag-button').tipTip({
				defaultPosition: 'bottom',
				maxWidth: '300px'
			});
		}

		return this;
	},
});

var Tags = Backbone.Collection.extend({
	model: Tag,
	comparator: function (tag) {
		return tag.get('name');
	},
	tagNameList: function () {
		return this.pluck('name');
	}
});

var Category = Backbone.Model.extend({
	defaults: {
		collapsed: true,
		hidden: false
	},
	getAppStats: function () {
		var apps = this.get('apps'),
			visible_apps = apps.where({
				hidden: false
			});
		return {
			// TODO https://github.com/documentcloud/underscore/issues/648
			num_selected: _.filter(visible_apps, function (app) {
				return app.get('selected');
			}).length,
			num_total: apps.length,
			num_visible: visible_apps.length
		};
	}
});

var Categories = Backbone.Collection.extend({
	model: Category,
	comparator: function (cat) {
		return cat.get('name').toLowerCase();
	}
});

var CategoryView = Backbone.View.extend({
	template: getTemplate('_category'),
	className: 'category',

	initialize: function (opts) {
		this.model.set('type', opts.type);
		this.model.set('disabled', opts.disabled);

		this.model.on('change:collapsed', this.toggleContents, this);

		this.model.on('change:hidden', this.toggleVisibility, this);

		// re-render the category header row to update its checkbox and stats
		this.model.get('apps').on('change',
			_.debounce(this.renderHeader, 100), this);

		this.model.get('apps').on('change:hidden',
			_.debounce(function () {
				this.hideAndOrCollapse();
				this.stripeAppRows();
			}, 100), this);

		// Remove hover in response to keyboard-based app filtering.
		// Necessary since this lets you shift (and hide) categories
		// w/o moving the mouse, which can lead to stale hover styling.
		window.dispatcher.on('browser:filter:keyboard', function () {
			this.$categoryEl.removeClass('hover');
		}, this);
	},

	events: {
		// toggle selection for all apps in this category
		'click .cat-checkbox': function (e) {
			this.model.get('apps').each(function (app) {
				if (!app.get('hidden')) {
					app.set('selected', e.target.checked);
				}
			});
		},
		'click .category-header': function (e) {
			if (e.target.className != 'cat-checkbox') {
				var collapse = !this.model.get('collapsed');
				this.model.set('collapsed', collapse);
				if (!collapse) {
					$("html,body").animate({
						scrollTop: this.$categoryEl.offset().top - 50
					});
				}
			}
		},
		'mouseenter .apps': 'hover',
		'mouseleave .apps': 'hover'
	},

	render: function () {
		//console.log('rendering %s category ...', this.model.get('id').toUpperCase());

		this.$el.html('<div class="category-header"></div><div class="category-apps"><div class="apps"></div></div>');

		this.$categoryEl = this.$('.category-header');
		this.$appsEl = this.$('.apps');
		this.$appsContainer = this.$('.category-apps');

		// this.$appsEl.toggle(!this.model.get('collapsed'));
		this.$appsContainer.toggle(!this.model.get('collapsed'));

		return this.renderHeader();
	},

	renderHeader: function () {
		//console.log('rendering %s category header ...', this.model.get('id').toUpperCase());

		var stats = this.model.getAppStats();

		this.model.set('all_selected', stats.num_visible == stats.num_selected);

		this.$categoryEl.html(this.template(
			_.extend({}, this.model.toJSON(), { t_blocking_summary: t_blocking_summary }, stats)
		));

		// setting indeterminate in the HTML template doesn't seem to work
		this.$categoryEl.find('.cat-checkbox').prop('indeterminate',
			stats.num_visible != stats.num_selected && !!stats.num_selected);

		if (!Ghostery.MOBILE_MODE) {
			this.$('.category-name').tipTip({
				defaultPosition: 'right',
				maxWidth: '300px'
			});
		}

		return this;
	},

	renderApps: function () {
		if (this.$appsEl.children().length === 0) {
			//console.log('rendering %s category apps ...', this.model.get('id').toUpperCase());
			var disabled = (this.model.get('type') == 'lsos') && this.model.get('disabled');

			this.model.get('apps').each(function (app) {
				this.$appsEl[0].appendChild((new AppView({
					model: app,
					disabled: disabled
				})).render().el);
			}, this);

			this.stripeAppRows();
		}

		this.$appsContainer.slideDown();

		return this;
	},

	hover: function (e) {
		this.$categoryEl.toggleClass('hover', e.type == 'mouseenter');
	},

	toggleContents: function () {
		this.renderHeader();

		if (this.model.get('collapsed')) {
			this.$appsContainer.slideUp();
		} else {
			this.renderApps();
		}
	},

	toggleVisibility: function () {
		if (this.model.get('hidden')) {
			this.model.set('collapsed', true);
			this.$el.hide();
		} else {
			this.renderHeader();
			this.$el.show();
		}
	},

	stripeAppRows: function () {
		//console.log('striping %s category ...', this.model.get('id').toUpperCase());

		this.$appsEl.find('.app').not('.hidden')
			.removeClass('alt-row')
			.filter(':even').addClass('alt-row');
	},

	hideAndOrCollapse: function () {
		var stats = this.model.getAppStats(),
			all_apps_hidden = stats.num_visible === 0,
			all_apps_shown = stats.num_total == stats.num_visible;

		this.model.set('hidden', all_apps_hidden);
		this.model.set('collapsed', all_apps_hidden || all_apps_shown);
	}
});

var AppBrowser = Backbone.View.extend({
	initialize: function (opts) {
		// TODO this.categories vs. this.get('categories')
		this.categories = opts.categories;
		this.type = (opts.el.selector.indexOf('trackers') >= 0) ? 'trackers' : 'lsos'; // TODO FF: improve

		this.new_app_ids = opts.new_app_ids;

		this.initializeTags();

		this.categories.on('reset', function () {
			this.$('#' + this.type).empty();
			this.addAllCategories();
		}, this);

		this.addAllCategories();

		// show block default message only if the option is off at this point
		if ($('#block-by-default').length === 0 || $('#block-by-default')[0].checked === true) {
			block_default_shown = true;
		}

		this.categories.on('change:all_selected', function () {
			if (!block_default_shown) {
				var globalAll = true;
				this.categories.forEach(function (category) {
					var stats = category.getAppStats();
					if ((stats.num_visible != stats.num_selected) ||
						(stats.num_visible != stats.num_total) ||
						(stats.num_selected != stats.num_total)) {
						globalAll = false;
					}
				});

				if (globalAll) {
					this.notifyDefaultBlock();
				}
			}
		}, this);

		this.$type_filter = $('#' + this.type + '-app-list-filter-type');
		this.$name_filter = $('#' + this.type + '-app-list-filter-name');
		this.$tag_filter = $('#' + this.type + '-app-list-filter-tag');
	},

	initializeTags: function () {
		// TODO add tags to lsos
		this.tags = TAGS;
		this.hasTagsSelected = false;
		this.addAllTags();

		if (this.type === 'trackers') {
			// TODO fix AppBrowser creation to remove need for TAGS
			this.tags.on('change:selected', function () {
				if (this.tags.some(function (tag) {
					return tag.get('selected');
				})) {
					this.hasTagsSelected = true;
				} else {
					this.hasTagsSelected = false;
				}
				this.filter();
			}, this);
		}
	},

	getVisible: function () {
		return this.categories.where({ hidden: false });
	},

	notifyDefaultBlock: function () {
		if (block_default_shown) {
			return;
		}

		$('#block-default-message-container').fadeIn({
			duration: 'fast',
			complete: function () {
				$('#block-by-default')[0].checked = true;
			}
		});

		$('#block-default-disable').click(function (e) {
			$('#block-default-message-container').fadeOut({
				duration: 'fast',
				complete: function () {
					$('#block-by-default')[0].checked = false;
					block_default_shown = true;
				}
			});
			e.preventDefault();
		});

		$('#block-default-close').click(function (e) {
			$('#block-default-message-container').fadeOut({
				duration: 'fast',
				complete: function () {
					$('#block-by-default')[0].checked = true;
					block_default_shown = true;
				}
			});
			e.preventDefault();
		});
	},

	events: function () {
		var events = {
			'click #app-list-reset-search, #filter-clear': function (e) {
				e.preventDefault();
				// TODO why doesn't changing the select box trigger its change event?
				this.$type_filter.val('all');
				this.$name_filter.val('');
				this.tags.invoke('set', { selected: false });
				this.filter();
			},
			'click #expand-all': function (e) {
				e.preventDefault();
				this.getVisible().forEach(function (category) {
					category.set('collapsed', false);
				});
			},
			'click #collapse-all': function (e) {
				e.preventDefault();
				this.getVisible().forEach(function (category) {
					category.set('collapsed', true);
				});
			},
			'click #select-all': function (e) {
				e.preventDefault();
				this.getVisible().forEach(function (category) {
					category.get('apps').each(function (app) {
						if (!app.get('hidden')) {
							app.set('selected', true);
						}
					});
				});
			},
			'click #select-none': function (e) {
				e.preventDefault();
				this.getVisible().forEach(function (category) {
					category.get('apps').each(function (app) {
						if (!app.get('hidden')) {
							app.set('selected', false);
						}
					});
				});
			},
			'click #more-tags, #less-tags': function (e) {
				e.preventDefault();
				$("#tag-list-" + this.type).toggleClass('small', 300);
			}
		};

		events['change #' + this.$type_filter[0].id] = this.filter;
		events['keyup #' + this.$name_filter[0].id] = _.debounce(function () {
			window.dispatcher.trigger('browser:filter:keyboard');
			this.filter();
		}, 300);

		// TODO reimplement searching for tags
		//$.widget("custom.specialcomplete", $.ui.autocomplete, {
		//	_renderMenu: function (ul, items) {
		//		var that = this;
		//		$.each(items, function (index, item) {
		//			if (item.value === "tag_header") {
		//				ul.append($("<li>", { class: "ui-autocomplete-category", text: item.label }));
		//			} else {
		//				that._renderItemData(ul, item);
		//			}
		//		});
		//	}
		//});

		//var that = this;

		//this.$name_filter.specialcomplete({
		//	source: tagNameList,
		//	minLength: 0,
		//	autoFocus: true,
		//	response: function (e, ui) {
		//		var value = $(e.target).val();

		//		if (ui.content.length !== 0) {
		//			ui.content.unshift({ value: "tag_header", label: "Tags" });
		//		}

		//		if (value !== "") {
		//			ui.content.unshift({ value: "app_search", label: "Search tracker names for '" + value + "'" });
		//		}
		//	},
		//	open: function () {
		//		$(".ui-menu").width($("#trackers-app-list-filter-name").width());
		//	},
		//	select: function (e, ui) {
		//		e.preventDefault();
		//		var value = ui.item.value;
		//		if (value === "app_search") {
		//			that.filter();
		//		} else {
		//			var tag = TAGS.where({ name: ui.item.value })[0];
		//			$("#trackers-app-list-filter-name").val("");
		//			tag.set('selected', true);
		//		}
		//	},
		//	messages: {
		//		noResults: '',
		//		results: function () { return ""; }
		//	},
		//	// Stop autocomplete from replacing input value
		//	focus: function (e) {
		//		e.preventDefault();
		//	}
		//});
		//this.$name_filter.focus(function () {
		//	$(this).data("customSpecialcomplete").search($(this).val());
		//});

		return events;
	},

	// TODO show a spinner when searching?
	filter: function () {
		var hide,
			hasTagsSelected = this.hasTagsSelected,
			type = this.$type_filter.val(),
			name = this.$name_filter.val(),
			new_app_ids = this.new_app_ids,
			total_visible = 0,
			selected_tags,
			filter_string;

		this.categories.each(function (category) {
			category.get('apps').each(function (app) {
				hide = false;

				if (!hide && type != 'all') {
					if (type == 'unblocked' && app.get('selected')) {
						hide = true;
					} else if (type == 'blocked' && !app.get('selected')) {
						hide = true;
					} else if (type == 'new' && new_app_ids && new_app_ids.indexOf(app.get('id')) == -1) {
						hide = true;
					}
				}

				if (!hide && name !== '' && app.get('name').toLowerCase().indexOf(name.toLowerCase()) == -1) {
					hide = true;
				}

				if (!hide && hasTagsSelected && app.get('tags').every(function (tag) { return !tag.get('selected'); })) {
					hide = true;
				}

				total_visible += hide ? 0 : 1;

				app.set('hidden', hide);
			});
		});

		if (name !== "" || hasTagsSelected) {
			selected_tags = TAGS.reduce(function (memo, tag) {
				if (tag.get('selected')) {
					memo.push(tag.get('name'));
				}
				return memo;
			}, []);

			if (name !== "" && hasTagsSelected) {
				filter_string = i18n.t('blocking_filter_name_tags', total_visible, name, selected_tags.join(", "));
			} else if (name !== "") {
				filter_string = i18n.t('blocking_filter_name', total_visible, name);
			} else if (hasTagsSelected) {
				filter_string = i18n.t('blocking_filter_tags', total_visible, selected_tags.join(", "));
			}

			$('#filter-desc-text').html(filter_string);
			$('#filter-desc').show(300);
		} else {
			$('#filter-desc').hide(300);
		}
	},

	addCategory: function (category) {
		var type = this.type;
		this.$('#' + this.type).append(
			(new CategoryView({
				model: category,
				type: type,
				disabled: (type == 'lsos') && (Ghostery.incompatibleAddons !== false)
			})).render().el
		);

		// re-render on app selection changes to update stats
		category.get('apps').on('change:selected', _.debounce(this.render, 100), this);

		// show "no results" when all categories are hidden; hide it otherwise
		category.on('change:hidden', function () {
			var visible = !!this.getVisible().length;
			// TODO link to http://www.ghostery.com/database/changelog maybe when filtering for "new" trackers and there is nothing to show
			// TODO https://getsatisfaction.com/ghostery/topics/update_notices_but_no_new_trackers
			$('#' + this.type).toggle(visible);
			$('#no-results-' + this.type).toggle(!visible);
			$('#app-toggles-' + this.type).toggle(visible);
		}, this);
	},

	addAllCategories: function () {
		this.categories.each(function (cat) {
			this.addCategory(cat);
		}, this);

		// TODO should this be triggered by an event instead?
		this.render();
	},

	addAllTags: function () {
		var $tagList = this.$('#tag-list-' + this.type),
			$moreTags = $('<a>', { id: 'more-tags',
				href: '#',
				text: i18n.t('tags_more') }),
			$lessTags = $('<a>', { id: 'less-tags',
				href: '#',
				text: i18n.t('tags_less') });

		$tagList.html('').append($moreTags, $lessTags);

		this.tags.each(function (tag) {
			// Kill tags with no trackers
			if (tag.get('count') === 0) {
				return;
			}

			$tagList.append(
				(new TagView({
					model: tag
				})).render().el
			);
		}, this);
	},

	render: function () {
		var num_selected = 0,
			num_total = 0;

		this.categories.each(function (category) {
			var apps = category.get('apps');
			num_selected += apps.where({ selected: true }).length;
			num_total += apps.length;
		});

		$('#block-status-' + this.type).html(t_blocking_summary(num_selected, num_total, this.type));
	},

	getSelectedAppIds: function () {
		return this.categories.chain()
			// get all the selected app IDs for each category
			.map(function (cat) {
				// TODO https://github.com/documentcloud/underscore/issues/648
				return cat.get('apps').chain().filter(function (app) {
					return app.get('selected');
				}).pluck('id').value();
			})
			// into a single array
			.flatten()
			// convert the array to a hash
			.reduce(function (memo, app_id) {
				memo[app_id] = 1;
				return memo;
			}, {})
			.value();
	}
});

function getCategories(bugdb, selected_app_ids, tagDb, isLSO) {
	// TAGS only needs to be created once, trackers are first
	if (!isLSO) {
		TAGS = new Tags(_.values(tagDb.list));
	}
	// tagNameList = TAGS.updateTagNameList();

	// bugdb.apps is an object indexed by app IDs
	return (_.chain(bugdb.apps)
			// convert to an array of objects
		.map(function (app, app_id) {
			return {
				// with app IDs as int properties
				id: +app_id,
				name: app.name,
				cat: app.cat,
				tags: app.tags
			};
		})
		// break it up by category
		.groupBy('cat')
		// convert to an array of category objects
		.map(function (apps, cat_name) {
			return {
				id: cat_name,
				name: i18n.t("category_" + cat_name),
				apps: new Apps(
					// create an array of app objects
					_.map(apps, function (app) {
						return {
							id: app.id,
							name: app.name,
							userCreated: app.userCreated,
							category: cat_name,
							tags: new Tags(_.map(app.tags, function (tag_id) {
								// TODO better place/way to get count for tags?
								var tagModel = TAGS.get(tag_id);
								// Missing tags can break browser creation
								if (tagModel) {
									tagModel.incrementCount();
								}
								return tagModel;
							})),
							selected: selected_app_ids.hasOwnProperty(app.id)
						};
					})
				)
			};
		})
	.value());
}
