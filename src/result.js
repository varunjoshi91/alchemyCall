/**
  * @copyright      2014 Alchemy API - All rights reserved.
  * @copyright        1.0.0
  *
  * @copyright    Demo library RESULTS area
  *
  */

var Results = ( function() {

  var self = this, // Set scope
      $el, $container, $endpoints, $endpoint_divs, $endpoint_details, $endpoint_details_divs,
      $defaults_endpoints_length, $title, $results_formats, $results_formats_btns,
      loadedCount = 0,
      targeted_sentiment_zero = {loaded: 0, data: []},
      targeted_sentiment,
      titleTpl = Handlebars.compile('<a href="http://www.alchemyapi.com/products/features/{{url}}" target="_blank">Click here to learn more about <b>{{help}}</b></a>.');

  // PUBLIC METHODS =================================================
  self.init = function(el) {
    targeted_sentiment = {loaded: 0, failed: 0, data: []}; //Reset this special case
    $el = el; // Set target element to a class-wide var
    init_dom(); // Init HTML
    init_els(); // Init JS objs from elements
    bind_events();
    set_endpoints_length(); // Set this for loaded indication
  };

  self.load = function(dataObj) {
    var endpoint = dataObj.endpoint,
        $endpoint = get_endpoint(endpoint),
        $endpoint_detail = get_endpoint_detail($endpoint),
        data = dataObj.data;

    set_endpoint_progress(endpoint);

    bad_lang = endpoint === 'language' && data.language === 'unknown';

    if (data.hasOwnProperty('status') && data.status === 'ERROR' || bad_lang) {
        if (data.statusInfo === 'unsupported-text-language' || bad_lang) {
            $endpoint_detail.html('<div class="error">This feature is not currently supported for this language</div>');
        }
        else {
            $endpoint_detail.html('<div class="error">'+endpoint+' returned error: '+data.statusInfo+'</div>');
        }

        if (endpoint === 'entities' || endpoint === 'keywords') {
            targeted_sentiment.failed++;

            if (targeted_sentiment.failed == 2)
            {
                self.load({endpoint: 'sentiment_targeted', data: data});
            }
        }
        return;
    }

    if (endpoint == 'relations') {
      init_relations_detail_dom(endpoint, $endpoint_detail, data);
    }
    else {
      // Massage data if required
      switch(endpoint) {
        case 'entities':
        case 'keywords':
          // Increment counter
          targeted_sentiment.loaded++;
          // Map data
          var datamap = $.map(data[endpoint], function(item, i) {
            return {
              text: item.text,
              type: (endpoint == 'entities') ? 'Entity' : 'Keyword',
              sentiment: item.sentiment
            };
          })
          // Merge into our data obj
          $.merge(targeted_sentiment.data, datamap);
          // When we have hit both endpoints, load sentiment_targeted
          if ((targeted_sentiment.loaded + targeted_sentiment.failed) == 2) {
            // console.log({endpoint: 'sentiment_targeted', data: targeted_sentiment.data})
            self.load({endpoint: 'sentiment_targeted', data: {sentiment_targeted: targeted_sentiment.data}});
          }
          break;
        case 'text':
          data.text = parse_text(data.text);
          break;
        case 'sentiment':
          data.docSentiment = [data.docSentiment];
          break;
        case 'author':
        case 'title':
          if (data[endpoint] == '')
            data = [data[endpoint]];
          break;
      }
      init_endpoint_detail_dom(endpoint, $endpoint_detail, data);
    }
  };

  self.reset = function() {
    self.init($el);
  };

  self.toggle = function(show, userClicked) {
    if (show)
      $container.fadeIn(DELAY);
    else
      $container.fadeOut(DELAY, function() {
        if (!userClicked) self.reset();
      });
  };

  self.set_active_endpoint = function(endpoint) {
    var $endpoint = $endpoints.find('[data-is="'+endpoint+'"]'),
        $endpoint_detail = get_endpoint_detail($endpoint),
        results_config = DEFAULTS['endpoints'][endpoint]['resultsConfig'];

    $endpoint_divs.removeClass('loaded active');
    $endpoint.addClass('active');

    $title.html(titleTpl({help: results_config['helpText'], url: results_config['helpUrl']}));

    $endpoint_details_divs.removeClass('active');
    $endpoint_detail.addClass('active');
    set_active_format_view($endpoint_detail);

    resize_iframe();
  };

  // // PRIVATE METHODS ==============================================
  var init_dom = function() {
    var config = get_config(), //Get base config
        endpoints = {endpoints: DEFAULTS.endpoints};

    $.extend(config, endpoints); // Add endpoints to config for iteration

    Handlebars.registerHelper('endpoint', function(key, label) {
      return '<div data-is="'+key+'" class="endpoint">'+label+'</div>';
    });

    Handlebars.registerHelper('endpointDetail', function(key, label) {
      return '<div data-is="'+key+'_detail" class="endpoint_detail">'+Handlebars.helpers.element_loader(16, 'Loading '+label+'...')+'</div>';
    });

    Handlebars.registerHelper('resultFormats', function() {
      var formatsArr = ['Visual','JSON','API'],
          html = '';
      $.each(formatsArr, function(i, format) {
        var selected = i == 0 ? 'selected' : ''
        html += '<span data-is="'+format+'" class="btn '+selected+'">'+format+'</span>'
      });
      return html;
    });

    var tpl = '<div class="container" style="display: none;">'+
      '<div class="titlebar">'+
        '<div data-is="results_title" class="a">{{element_loader 16 "Loading endpoints..."}}</div>'+
        '<div data-is="results_formats">{{#resultFormats}}{{/resultFormats}}</div>'+
      '</div>'+
      '<div data-is="endpoints">'+
        '{{#each endpoints}}'+
          '{{#if display}}'+
            '{{#endpoint @key label}}{{/endpoint}}'+
          '{{else}}'+
            '{{#if showNotLoad}}'+
              '{{#endpoint @key label}}{{/endpoint}}'+
            '{{/if}}'+
          '{{/if}}'+
        '{{/each}}'+
      '</div>'+
      '<div data-is="endpoint_details">'+
        '{{#each endpoints}}'+
          '{{#if display}}'+
            '{{#endpointDetail @key label}}{{/endpointDetail}}'+
          '{{else}}'+
            '{{#if showNotLoad}}'+
              '{{#endpointDetail @key label}}{{/endpointDetail}}'+
            '{{/if}}'+
          '{{/if}}'+
        '{{/each}}'+
      '</div>'+
    '</div>';

    // Use Handlebars to generate HTML based on config
    tpl = Handlebars.compile(tpl);
    $el.html(tpl(config));
  };

  var init_endpoint_detail_dom = function(endpoint, $endpoint_detail, data) {

    var config = DEFAULTS['endpoints'][endpoint]['resultsConfig'],
        tableConfig = config.table,
        dataKey = tableConfig.dataKey,
        isFlatData = dataKey == '',
        rows = isFlatData ? [data] : data[tableConfig.dataKey],
        results = {};

    // Remove empty string for row data as in the case of api error
    if (rows.length == 1 && rows[0] == '')
      rows = [];

    $.extend(results, {rows: rows, columns: tableConfig.columns});

    Handlebars.registerHelper('add_vis', function(data) {
      if (config.vis != '')
        return '<div data-is="'+endpoint+'_vis"></div>'
      else
        return '';
    });

  Handlebars.registerHelper('parse_result', function(rowData) {
    var htmlStr ='';

    $.each(results.columns, function(i, column) {
      var configVal = column.val,
          cellData = rowData[configVal];

      if (!$.isArray(configVal)) {
        if (configVal == 'sentiment' && typeof(cellData) !== 'undefined') {
          cellData = (typeof(cellData.mixed) !== 'undefined') ? 'mixed' : cellData.type;
          cellData = '<span style="color:'+SENTIMENT_COLORS[cellData]+'">'+cellData+'</span>';
        }
        if (typeof(column.dataKey) !== 'undefined') {
          var dataKey = column.dataKey,
              valueObj = rowData[dataKey];

          cellData = '&nbsp;';
          if (typeof(valueObj) !== 'undefined') {
            valueObj = valueObj[configVal];
            if (typeof(valueObj) !== 'undefined') {
              if ($.isArray(valueObj)) {
                if (valueObj.length > 0){
                  cellData = '';
                  $.each(valueObj, function(j, value) {
                    cellData += value+'<br/>';
                  });
                }
              }
            }
          }
        }
        if (typeof(column.type) !== 'undefined') {
          if (column.type == 'link') {
            var href = cellData;
            if (href.match('http://') == null) {
              href = 'http://'+href;
            }
            cellData = '<a href="'+href+'" target="_blank">'+cellData+'</a>';
          }
        }

        if (typeof(cellData) === 'undefined' || cellData == '') cellData = '&nbsp;';

        htmlStr += '<div class="cell" data-is="'+configVal+'">'+cellData+'</div>';
      }
      else {
        htmlStr += '<div class="cell" data-is="'+configVal+'">';
        if (typeof(column.dataKey) !== 'undefined') {
          var dataKey = column.dataKey,
              valueObj = rowData[dataKey];
          if (typeof(valueObj) !== 'undefined')
            rowData = valueObj;
        }
        $.each(configVal, function(i, item) {
          if (typeof(rowData[item]) !== 'undefined')
            htmlStr += '<a href="'+rowData[item]+'" target="_blank">'+item+'</a><br/>'
        });
        htmlStr += '</div>';
      }
    });

    return htmlStr;
  });

  var tpl = '<div class="results_container">'+
    '{{#if rows.length}}'+
      '{{#add_vis this}}{{/add_vis}}'+
      '<div class="row head">'+
        '{{#each columns}}'+
          '<div data-is="{{val}}" class="cell head">{{label}}</div>'+
        '{{/each}}'+
      '</div>'+
      '{{#each rows}}'+
        '<div class="row">'+
          '{{#parse_result this}}{{/parse_result}}'+
        '</div>'+
      '{{/each}}'+
    '{{else}}'+
      '<div class="no_data">No '+config.helpText+' detected</div>'+
    '{{/if}}'+
  '</div>'+
  '<div class="json_container">'+
    JSON.stringify(data, null, 2)+
  '</div>';

  // Use Handlebars to generate HTML based on config
  tpl = Handlebars.compile(tpl);

  console.log(endpoint);

  l_tpl = null;
  try
  {
    l_tpl = tpl(results);
  }
  catch (err)
  {
    console.log("ERROR: " + err.message);
    return;
  }


  $endpoint_detail.html(l_tpl);

  if (config.vis != '')
    init_vis(config.vis, endpoint, data);

  if (typeof(config.clickable) !== 'undefined') {
    var $clickables = $endpoint_detail.find('.row:not(.head)'),
        clickConfig = config.clickable;

    $clickables.data('config',{target: clickConfig.target, pull: clickConfig.pull});
    $clickables.addClass('clickable');
    $clickables.on('click', highlight_row);
  }
  };

  var init_relations_detail_dom = function(endpoint, $endpoint_detail, data) {


    var config = DEFAULTS['endpoints'][endpoint]['resultsConfig'],
        tableConfig = config.table,
        dataKey = tableConfig.dataKey,
        relations_raw = data[tableConfig.dataKey],
        textObjArr = ['subject','action','object','location'];

      {
      // Massage the data
      var relations = [];
      $.each(relations_raw, function(i, relation) {
        var parts = [],
            verbs,
            entities = [];

        for (key in relation) {
          var part = relation[key];

          // Build parts
          if (typeof(part.text) !== 'undefined') {
            var sentiment = '';
            if (typeof(part.sentiment) !== 'undefined')
              sentiment += part.sentiment.type + '&nbsp;(relational)';
            if (typeof(part.sentimentFromSubject) !== 'undefined') {
              if (sentiment.length > 0)
                sentiment += ', ';
              sentiment += part.sentimentFromSubject.type + '&nbsp;(directional)';
            }
            var keywords = [];
            if (typeof(part.keywords) !== 'undefined' && part.keywords.length > 0) {
              keywords = $.map(part.keywords, function(word, i) {
                return word.text;
              });
              if (keywords.length > 0)
                keywords.join(', ');
            }
            parts.push({
              text: part.text,
              part: key,
              sentiment: sentiment,
              keywords: keywords
            });
          }
          // Build verbs
          if (key == 'action') {
            verbs = [{
              text: part.text,
              lemmatized: part.lemmatized,
              verbtext: part.verb.text,
              verbtense: part.verb.tense,
              verbnegated: (typeof(part.verb.negated) !== 'undefined') ? 'Yes' : 'No'
            }];
          }
          // Build entities
          if (typeof(part.entities) !== 'undefined') {
            $.each(part.entities, function(i, entity) {
              var disambiguated = entity.disambiguated;
              entities.push({
                entity: entity.text,
                part: key,
                type: entity.type,
                subtypes: (typeof(disambiguated) !== 'undefined' && typeof(disambiguated.subType) !== 'undefined') ? entity.disambiguated.subType.join(', ') : '',
                disambiguated: (typeof(disambiguated) !== 'undefined') ? entity.disambiguated : ''
              });
            });
          }
        }

        var textObj = {};
        $.each(textObjArr, function(i, textStr) {
          if (typeof(relation[textStr]) !== 'undefined')
            textObj[textStr] = relation[textStr].text;
        });

        relations.push({
          text: textObj,
          sentence: relation.sentence,
          parts: parts,
          verbs: verbs,
          entities: entities
        });
      });

      Handlebars.registerHelper('collapser', function(relation) {
        var typesArr = ['subject', 'action', 'object', 'location'],
            str = '';
        $.each(typesArr, function(i, type) {
          if (typeof(relation.text[type]) !== 'undefined')
            str += ' '+relation.text[type];
        });
        return '<div class="collapser a"><span class="twist">&nbsp;</span>'+str+'</div>';
      });

      Handlebars.registerHelper('sentence', function(relation) {
        return '<div class="sentence">'+
          '<h2>Extracted Sentence</h2>'+
          relation.sentence+
        '</div>';
      });

      Handlebars.registerHelper('head', function(row) {
        return '<h2>'+tableConfig.rows[row].label+'</h2>';
      });

      Handlebars.registerHelper('headRow', function(row) {
        var cols = tableConfig.rows[row].columns,
            html = '';
        $.each(cols, function(i, col) {
          html += '<div data-is="'+col.val+'" class="cell head a">'+col.label+'</div>';
        });
        return html;
      });

      Handlebars.registerHelper('rows', function(rowData, rowType) {
        var cols = tableConfig.rows[rowType].columns,
            html = '';

        $.each(rowData[rowType], function(i, data) {
          html += '<div class="row">';
          $.each(cols, function(i, col) {
            var value = data[col.val];
            value = value == '' ? '&nbsp;' : value;

            if ($.isArray(col.val)) {
              value = '';
              if (typeof(col.dataKey) !== 'undefined') {
                var dataKey = col.dataKey;
                $.each(col.val, function(i, key) {
                  if (typeof(data[dataKey]) !== 'undefined') {
                    if (typeof(data[dataKey]) !== 'undefined' && data[dataKey] != '') {
                      if (typeof(data[dataKey][key]) !== 'undefined')
                        value += '<a href="'+data[dataKey][key]+'" target="_blank">'+key+'</a><br/>';
                      else
                        value += '';
                    }
                    else
                      value += '';
                  }
                  else
                    value += '';
                });
              }
            }

            html += '<div class="cell">'+value+'</div>';
          });
          html += '</div>';
        });

        return html;
      });

      Handlebars.registerHelper('table', function(relation) {
        var relationTypesArr = ['parts', 'verbs', 'entities'],
            html = '';
        $.each(relationTypesArr, function(i, type) {
          html += tableTpl({type: type, data: relation});
        });
        return html;
      });

      var tpl = '<div data-is="relation">'+
        '{{#collapser this}}{{/collapser}}'+
        '<div class="collapsible">'+
          '{{#sentence this}}{{/sentence}}'+
          '{{#table this}}{{/table}}'+
        '</div>'+
      '</div>';
      tpl = Handlebars.compile(tpl);

      var tableTpl = '<div class="relation-data">'+
        '{{#head type}}{{/head}}'+
        '<div class="relation-table">'+
          '<div class="row">'+
            '{{#headRow type}}{{/headRow}}'+
          '</div>'+
          '{{#rows data type}}{{/rows}}'+
        '</div>'+
      '</div>';
      tableTpl = Handlebars.compile(tableTpl);

      var html = '<div class="results_container" style="display: block;">';
      if (relations.length > 0) {
        $.each(relations, function(i, relation) {
          html += tpl(relation);
        });
      }
      else {
        html += '<div class="no_data">No '+config.helpText+' detected</div>';
      }
      html += '</div><div class="json_container">'+JSON.stringify(data, null, 2)+'</div>';
      $endpoint_detail.html(html);

      //$endpoint_detail.first('.collapser').toggle('slow');
      $endpoint_detail.find('.collapser').on('click', toggle_collapsible);
    }
  };

  var init_els = function() {
    $container = $el.find('.container');
    $endpoints = $el.find('[data-is="endpoints"]');
    $endpoint_divs = $el.find('[data-is="endpoints"] div');
    $endpoint_details = $el.find('[data-is="endpoint_details"]');
    $endpoint_details_divs = $el.find('[data-is="endpoint_details"] div');
    $title = $el.find('[data-is="results_title"]');
    $results_formats = $el.find('[data-is="results_formats"]');
    $results_formats_btns = $el.find('[data-is="results_formats"] span');
  };

  var init_vis = function(vis, endpoint, data) {
    if (vis == 'treemap') new Treemap(endpoint, data);
  }

  var bind_events = function() {
    $endpoint_divs.on('click', fetch_endpoint);
    $results_formats_btns.on('click', toggle_format);
  };

  var get_config = function() {
    // $.getJSON('/data/'+SOLUTION.toLowerCase()+'_analyze.json', callback);
    var config;
    $.each(DEFAULTS.solutions[SOLUTION].steps, function(i, step) {
      if (step.step == 'results') {
        config = step.config;
        return false; // exit loop
      }
    });
    return config;
  };

  var get_endpoint = function(endpoint) {
    return $endpoints.find('[data-is="'+endpoint+'"]');
  };

  var get_endpoint_detail = function($endpoint) {
    var endpoint = $endpoint.data('is');
    return $endpoint_details.find('[data-is="'+endpoint+'_detail"]');
  };

  var get_active_endpoint = function() {
    return $endpoints.find('.endpoint.active');
  };

  var set_endpoint_progress = function(endpoint) {
    var $endpoint = $endpoints.find('[data-is="'+endpoint+'"]');
    $endpoint.addClass('loaded');
    loadedCount ++;

    if ($defaults_endpoints_length == loadedCount) {
      loadedCount = 0;
      self.set_active_endpoint('entities');
      $results_formats.show().css('display', 'inline-block');
      $title.show().css('display', 'inline-block');
      Analyze.stop_loading();
    }
  };

  var set_endpoints_length = function () {
    var length = 0;
    $.each(DEFAULTS.endpoints, function(i, endpoint) {
      if (endpoint.display || typeof(endpoint.showNotLoad) !== 'undefined')
        length ++;
    });
    $defaults_endpoints_length = length;
  };

  var parse_text = function(text) {
    var doc = null;
    doc = document.createElement('html');
    doc.innerHTML = text;

    var links = doc.getElementsByTagName('a'),
        data = [];

    $.each(links, function(i, link) {
      var domain = link.hostname;
      data.push({provider: domain, link: link.href});
    });

    return data;
  };

  var set_active_format_view = function($endpoint_detail, format) {
    var active_format = (format) ? format : $results_formats.find('.selected').data('is'),
        $results = $endpoint_detail.find('.results_container'),
        $json = $endpoint_detail.find('.json_container');

    switch(active_format.toLowerCase()) {
      case 'visual':
        $json.hide();
        $results.show();
        break;
      case 'json':
        $results.hide();
        $json.show();
        break;
    }
  };

  var resize_iframe = function() {
    // Resize window if required
    top.postMessage(JSON.stringify({h:document.body.scrollHeight, w:document.body.scrollWidth}), '*');
  }

  // EVENT HANDLERS =================================================
  var fetch_endpoint = function(e) {
    var data_is = $(this).data('is');
    self.set_active_endpoint(data_is);
    _gaq.push(['_trackEvent', 'Dfuzr', 'View API Endpoint', data_is]);
  };

  var toggle_collapsible = function(e) {
    $(this).toggleClass('active');
    $(this).find('.twist').toggleClass('on'),
    $(this).siblings('.collapsible').toggle(DELAY, resize_iframe);
  };

  var toggle_format = function(e) {
    var format = $(this).data('is'),
        $endpoint = get_active_endpoint();

    if (format.toLowerCase() != 'api') {
      var $endpoint_detail = get_endpoint_detail($endpoint),
          $results = $endpoint_detail.find('.results_container'),
          $json = $endpoint_detail.find('.json_container');

      $results_formats_btns.removeClass('selected');
      $(this).addClass('selected');
      set_active_format_view($endpoint_detail, format);
    }
    else {
      var endpoint = $endpoint.data('is');
      window.open(DOCS_URL+DEFAULTS.endpoints[endpoint].docsUrl, '_blank');
    }
  };

  var highlight_row = function(e) {
    var clickConfig = $(this).data('config'),
        $targetCell = $(this).find('[data-is="'+clickConfig.target+'"]'),
        $targetSpan = $targetCell.find('span'),
        targetVal = $targetCell.text(),
        pullVal = $(this).find('[data-is="'+clickConfig.pull+'"]').text(),
        rowCells = $(this).find('.cell');

    if (rowCells.hasClass('row-highlight')) {
      $targetSpan.css('color', SENTIMENT_COLORS[targetVal]);
      rowCells.removeClass(targetVal+' row-highlight');
      Content.clear_highlight(pullVal);
    }
    else {
      $targetSpan.css('color', '#fff');
      rowCells.addClass(targetVal+' row-highlight'); // Highlight this specific row
      Content.highlight(pullVal, targetVal); // Highlight the words in place
    }
  };

  return this;

}).call({});