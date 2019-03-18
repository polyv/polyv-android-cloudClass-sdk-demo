if (typeof Object.assign != 'function') {
  // Must be writable: true, enumerable: false, configurable: true
  Object.defineProperty(Object, "assign", {
    value: function assign(target, varArgs) { // .length of function is 2
      'use strict';
      if (target == null) { // TypeError if undefined or null
        throw new TypeError('Cannot convert undefined or null to object');
      }

      var to = Object(target);

      for (var index = 1; index < arguments.length; index++) {
        var nextSource = arguments[index];

        if (nextSource != null) { // Skip over if undefined or null
          for (var nextKey in nextSource) {
            // Avoid bugs when hasOwnProperty is shadowed
            if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
              to[nextKey] = nextSource[nextKey];
            }
          }
        }
      }
      return to;
    },
    writable: true,
    configurable: true
  });
}

const words = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

const tmp = {

  parseString(str) {
    return str.replace(/</g, '&lt;').replace(/>/g, '&gt;').trim();
  },

  questionTypeTxt(type) {
    return type === 'C' ? '多选' : '单选';
  },

  isMultiSelect(type) {
    return type === 'C';
  },

  getStatusClass(type) {
    let cls = '';
    switch (type) {
      case 'success':
        cls = 'answer-right';
        break;
      case 'error':
        cls = 'answer-error';
        break;
      case 'checked':
        cls = 'answer-checked';
        break;
      case 'selectRight':
        cls = 'answer-right-select';
        break;
      case 'normal':
        cls = 'answer-normal';
        break;
      case 'noselect':
        cls = 'no-select';
        break;
      default:
    }

    return cls;
  },

  option({ ratio = '0', contain = '', type = '', index, count = '' }) {
    if(!contain) return '';
    return `
    <li class="${this.getStatusClass(type)}" data-options="${index}">
      <div class="answer-ratio" style="width:${ratio}"></div>
      <div class="answer-option">${index}. ${this.parseString(contain)}</div>
      <span class="answer-count">${count}</span>
    </li>
    `;
  },

  getOptions(options) {
    const selectsNumbers =  Object.keys(options)
    .filter((item) => item.indexOf('option') !== -1 && options[item])
    .length;
    let txt = '';
    for (let i = 0; i < selectsNumbers; i++) {
      txt += this.option( {
        contain: options[`option${i + 1}`],
        index: words[i],
        type:  options[`type${i + 1}`],
        ratio: options[`ratio${i + 1}`] || 0,
        count: options[`result${i + 1}`]
      })
    }
    return txt;
  },

  card(options) {
    const id = `id-${new Date().getTime()}`;
    const el = document.createElement('div');
    el.setAttribute('class', `answer-card ${options.tipsClass || ''}`);
    el.innerHTML = `<div class="answer-head"></div>
    <div class="answer-contain">
      <h3>【${this.questionTypeTxt(options.type)}】${this.parseString(options.title || options.name)}</h3>
      <ul class="answer-list">
        ${this.getOptions(options)}
      </ul>
    </div>
    <div class="answer-foot">
      <span id="${id}" class="answer-btn ${options.btnClass || ''}" data-type="${options.submitType}">${options.btn}</span>
    </div>
    <div>
      <span class="answer-close" data-type="close"></span>
    </div>`;

    return {el, id};
  },

  tips(txt) {
    const el = document.createElement('div');
    el.textContent = txt;
    el.classList.add('answer-tips');
    return el;
  }
};


window.knowAnswer = function() {
  window.WebViewJavascriptBridge.callHandler('knowAnswer');
};

window.chooseAnswer = function(dataString) {
  console.log(dataString);
  window.WebViewJavascriptBridge.callHandler('chooseAnswer', dataString);
}

class AnswerCard {

  updateNewQuestion(data) {
    if (data.type === 'S') {
      knowAnswer();
      return;
    }

    this.currentStatus = {
      id: data.questionId,
      type: 'commit'
    };

    this.selectOptions = [];
    const newData = Object.assign({
      submitType: 'commit',
      btn: '确定',
      tips: '答题卡'
    }, data);
    this.initElement(newData, true, data.questionId, tmp.isMultiSelect(newData.type));
  }

  hasChooseAnswer(select, data) {
    // 可在此处处理答题结果
    // 目前不处理
    return;
    if (data.type === 'S') {
      knowAnswer();
      return;
    }

    this.currentStatus = {
      id: data.questionId,
      type: 'close'
    };
    // await this.delay();
    // 没上次记录或者不是同一条问题不处理
    // const answerData = this.data[data.questionId] || {};
    data.result = data.data || data.result;
    const resultData = this.createResultData(select, data.result);
    const tipsData = this.getResultTips(select, data.result.answer, data.questionId);
    const newData = Object.assign({
      submitType: 'close',
      btn: '知道了',
      btnClass: 'answer-primary',
      tips: tipsData.tips,
      tipsClass: tipsData.tipsClass
    }, resultData, data, data.content);

    this.initElement(newData, false);
  }

  stopQuestion(data) {
    if (this.currentStatus.type !== 'commit' || this.currentStatus.id !== data.questionId) return;
    this.currentStatus.type = 'stop';
    const cls = tmp.getStatusClass('checked');
    this.btn.classList.remove('answer-primary');
    this.btn.textContent = '已截止';
    this.selectOptions = [];
    [...this.el.querySelectorAll(`.${cls}`)].forEach(item => {
      item.classList.remove(cls);
    });
  }

  initElement(newData, bindOption, questionId, isMultiSelect = false) {
    this.removeCard();
    const card = tmp.card(newData);
    this.el = card.el;
    this.wrap.appendChild(this.el);
    this.btn = document.getElementById(card.id);
    this.wrap.classList.add('answer-show');
    this.bindEvent(bindOption, questionId, isMultiSelect);
  }

  getResultTips(select, answer, id) {
    const result = select || '';
    let tips = '';
    let tipsClass = '';
    if (!result) {
      tips = '未作答';
    } else if (result === answer) {
      tips = '回答正确';
      tipsClass = tmp.getStatusClass('success');
    } else {
      tips = '回答错误';
      tipsClass = tmp.getStatusClass('error');
    }

    return {
      tips,
      tipsClass
    };
  }

  createResultData(select, result) {
    const obj = {};
    const selectOptions = select.split('');
    result.singleResult.forEach((item, index) => {
      const currentSelect = words[index];
      console.log(currentSelect);
      obj[`result${index + 1}`] = item;
      obj[`ratio${index + 1}`] = item / (result.total || 1) * 100 + '%';
      obj[`type${index + 1}`] = this.getAnswerType(result.answer, currentSelect, selectOptions.indexOf(currentSelect) !== -1, item);
    });

    return obj;
  }

  getAnswerType(answer, currentSelect, isSelect, ratio) {
    const isRight = answer.indexOf(currentSelect) !== -1;
    if (isRight && isSelect) {
      return 'selectRight';
    } else if(!ratio && isRight) {
      return 'noselect';
    }else if (isRight) {
      return 'success';
    } else if (!isRight && isSelect) {
      return 'error';
    } else {
      return 'normal';
    }
  }

  bindEvent(bindOption, questionId, isMultiSelect = false) {
    const _this = this;
    let currentEl = null;

    this.el.addEventListener('click', (event) => {
      const type = event.target.getAttribute('data-type');
      if (!type) return;
      if (type === 'close') {
        this.removeCard();
        knowAnswer();
      }

      if (type === 'commit') {
        this.commit(questionId);
      }
    });

    bindOption && this.el.addEventListener('click', (event) => {
      if (this.currentStatus.type === 'stop') return;
      let bool = true;
      let target = event.target;
      let option = null;
      while (bool) {
        option = target.getAttribute('data-options');
        if (option) {
          bool = false;
          break;
        }
        if (target === this.el) {
          bool = false;
          target = null;
          break;
        }
        target = target.parentNode;
        if(!target) break;
      }

      if (!option) return;

      if (isMultiSelect) {
        this.multiHandler(target, option);
      } else {
        currentEl = this.singleHandler(currentEl, target, option);
      }
    })
  }

  multiHandler(el, option) {
    const cls = tmp.getStatusClass('checked');
    const index = this.selectOptions.indexOf(option);
    if (index > -1) {
      this.selectOptions.splice(index, 1);
    } else {
      this.selectOptions.push(option);
    }

    if (this.selectOptions.length > 0) {
      this.btn.classList.add('answer-primary');
    } else {
      this.btn.classList.remove('answer-primary');
    }
    el.classList.toggle(cls);
  }

  singleHandler(currentEl, el, option) {
    const cls = tmp.getStatusClass('checked');
    if (currentEl) currentEl.classList.remove(cls);
    el.classList.add(cls);
    this.selectOptions = [option];
    this.btn.classList.add('answer-primary');
    return el;
  }

  sortResult(ary = []) {
    return ary.sort().join('');
  }

  commit(questionId) {
    const result = this.sortResult(this.selectOptions);
    if (!result) return;
    this.removeCard();

    this.showTips('提交成功', 1.5, () => {
      chooseAnswer({
        answerId: result,
        questionId
      });
    });
  }

  removeCard() {
    if (this.el) {
      this.wrap.removeChild(this.el);
      this.el = null;
      this.wrap.classList.remove('answer-show');
    }
  }

  showTips(txt, duration = 2, cb = function(){}) {
    const tips = tmp.tips(txt);
    this.wrap.appendChild(tips);
    this.wrap.classList.add('answer-show');
    if (duration) {
      setTimeout(() => {
        this.wrap.removeChild(tips);
        this.wrap.classList.remove('answer-show');
        cb();
      }, duration * 1000);
    }
  }

  constructor(wrap) {
    this.wrap = document.getElementById(wrap);
    this.isMobile = true;
    this.el = null;
    this.btn = null;
    this.selectOptions = [];
    this.currentStatus = {};
  }
}

const card = new AnswerCard('wrap');

const $log = document.getElementById('log');
const log = (txt) => {
  return;
  const a = document.createElement('div');
  a.textContent = txt;
  $log.appendChild(a);
};

window.updateNewQuestion = function(data) {
  card.updateNewQuestion(JSON.parse(data));
}

window.hasChooseAnswer = function(data) {
 try{
 var res = JSON.parse(data);
 }catch(e){
  log(e)
 }

  log(res.answerId);
  card.hasChooseAnswer(res.answerId, res.data);
};

window.extraQuestionMessageHandler = function(data) {
  var res = JSON.parse(data);
  if (res.type === 'S' || res.EVENT !== 'STOP_TEST_QUESTION') return;
  card.stopQuestion(res);
}

function connectWebViewJavascriptBridge(callback = function(){}) {
  log('注册');
  if (window.WebViewJavascriptBridge) {
     log('注册:type1');
     callback(WebViewJavascriptBridge)
  } else {
    log('注册:type2');
     document.addEventListener('WebViewJavascriptBridgeReady', function() {
        callback(WebViewJavascriptBridge)
      }, false);
   }
 }

connectWebViewJavascriptBridge(function(bridge) {
    log('注册');
   bridge.init(function(message, responseCallback) {});
   bridge.registerHandler('updateNewQuestion', function(data, responseCallback) {
       log('收到答题信息');
       responseCallback('收到答题信息');
       window.updateNewQuestion(data);
   });
   bridge.registerHandler('hasChooseAnswer', function(data, responseCallback) {
        log('收到答题结果'+ data);
       window.hasChooseAnswer(data);
        responseCallback('收到答题结果:'+data);
   });
   bridge.registerHandler('testQuestion', function(data, responseCallback) {
        log('收到额外消息'+ data);
       window.extraQuestionMessageHandler(data);
        responseCallback('收到额外消息:'+data);
   });
});