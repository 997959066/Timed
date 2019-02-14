layui.config({
	base : "js/"
}).use(['form','element','table','layer','laydate','laypage','jquery'],function(){
	var form = layui.form,
      layer = layui.layer,
      element = layui.element,
      $ = layui.jquery,
      laydate = layui.laydate,
      laypage = layui.laypage,
      table = layui.table;
  var pageNo = 1,
      pageSize = 10,
      tableList = [],
      count = 0;

  renderTable();
  function renderTable() {
    var id = $('#jobId').val();
    var jobName = $('#jobName').val();
    var jobStatus = $('#jobStatus').val();
    var concurrent = $('#concurrent').val();
    var execStatus = $('#execStatus').val();
    var parmas = {
          pageNo: pageNo,
          pageSize: pageSize
        };
    if(id) parmas.id = id;
    if(jobName) parmas.jobName = jobName;
    if(jobStatus) parmas.jobStatus = jobStatus;
    if(concurrent) parmas.concurrent = concurrent;
    if(execStatus) parmas.execStatus = execStatus;
    
    getList(parmas);
    renderTableData();
    laypage.render({
      elem: 'pagination',
      count: count,
      curr: pageNo,
      limit: pageSize,
      layout: ['count', 'prev', 'page', 'next', 'limit', 'skip'],
      jump: function(obj, first){
        if(!first) {
          pageNo = obj.curr,
          pageSize = obj.limit;
          parmas.pageNo = pageNo;
          parmas.pageSize = pageSize;
          getList(parmas);
          renderTableData();
        }
      }
    });
  }


  function getList(parmas) {
    $.ajax({
      type: 'get',
      url: api +'/scheduleJob/listByPage',
      dataType: 'json',
      data: parmas,
      async: false,
      success: function(data) {
//        if(data.code == 1) {
          tableList = data.list;
          count = data.rowCount;
          tableList.forEach(function(item, index) {
            if(item.expiredDate) item.expiredDate = item.expiredDate.replace(/(\.0)$/g, '');
            if(item.beginDate) item.beginDate = item.beginDate.replace(/(\.0)$/g, '');
            switch(item.jobStatus){
              case 0: item._jobStatus = '初始'; break;
              case 1: item._jobStatus = '启用'; break;
              case 2: item._jobStatus = '删除'; break;
              default: item._jobStatus = item.jobStatus;
            }
            switch(item.concurrent){
              case 0: item._concurrent = '禁用'; break;
              case 1: item._concurrent = '启用'; break;
              default: item._concurrent = item.concurrent;
            }
          /*  switch(item.linkType){
              case 1: item._linkType = '重复任务'; break;
              case 0: item._linkType = '无'; break;
              case 2: item._linkType = '任务定时通知'; break;
              case 3: item._linkType = '系统类型'; break;
              default: item._linkType = item.linkType;
            }*/
            switch(item.execStatus){
              case 1: item._execStatus = '正常'; break;
              case 2: item._execStatus = '错误'; break;
              case 3: item._execStatus = '异常'; break;
              default: item._execStatus = item.execStatus;
            }
          })
        /*}else {
          layer.msg(data.msg);
        }*/
      },
      error: function(data) {
        layer.msg(data.msg);
      }
    })
  }

  // 渲染 table
  function renderTableData() {
    table.render({
      elem: '#contentTable',
      height: 'full-' + handleH,
      data: tableList,
      limit: pageSize,
      cols: [[
        {type:'checkbox', fixed: 'left'},
        {field:'id', title: 'id', sort: true, width: 60, fixed: 'left'},
//        {field:'jobShowName', title: '任务名称-显示在页面', width: 170},
        {field:'jobName', title: '任务名称', width: 120},
//        {field:'jobGroup', title: '任务所属组', width: 100},
        {field:'_jobStatus', title: '任务状态', width: 90},
        {field:'jobDesc', title: '任务描述', width: 90},
        {field:'cronExpression', title: '任务时间表达式', width: 140},
        {field:'jobUrl', title: '掉用url', width: 400},
        {field:'targetMethod', title: '执行方法', width: 90},
        {field:'jobParam', title: '传入的参数', width: 110},
        {field:'jobHeader', title: '请求头', width: 110},
        {field:'beginDate', title: '定时任务开始时间', width: 170},
        {field:'expiredDate', title: '定时任务截至时间', width: 170},
        {field:'_concurrent', title: '是否并发启动', width: 120},
        {field:'targetObject', title: '目标执行的类', width: 300},
        {field:'retryCount', title: '失败后重试次数', width: 130},
        {field:'failCallbackUrl', title: '失败后回调url', width: 120},
        {field:'createDate', title: '创建时间 ', width: 170},
        {field:'updateDate', title: '更新时间', width: 170},
        {field:'execStatus', title: '任务执行状态', width: 120, templet: '#statusStyle', fixed: 'right'},
        {field:'right', title: '操作', width: 120, toolbar: '#handleTool', fixed: 'right'},
        {field:'right', title: '日志', width: 90, toolbar: '#logDetail', fixed: 'right'},
      ]]
    });
  }

  // 新增
  $('#add').on('click', function() {
    layer.open({
      type: 1,
      title: '新增',
      content: $('#addDialog'),
      area: ['720px', '510px'],
      btn: ['确定', '取消'],
      btnAlign: 'c',
      yes: function() {
        var parmas = getAddParmas();
//        parmas.createUser = JSON.parse(window.sessionStorage.getItem("currentUser")).id;
        $.ajax({
          type: 'post',
          url: api +'/scheduleJob/append',
          data: parmas,
          dataType: 'json',
          success: function(data) {
//            if(data.code == 1) {
              layer.msg("添加成功！");
              renderTable();

//            }else {
//              layer.msg(data.msg);
//            }
          },
          error: function(data) {
            layer.msg(data.msg);
          }
        })
        layer.closeAll();
      }
    });

    laydate.render({
      elem: '#addBeginDate',
      type: 'datetime'
    })
    laydate.render({
      elem: '#addExpiredDate',
      type: 'datetime'
    })

    $('#addId').val('');
    $('#addJobName').val('');
    $('#addJobStatus').val('');
    $('#addJobDesc').val('');
    $('#addCronExpression').val('');
    $('#addJobUrl').val('');
    $('#addTargetMethod').val('');
    $('#addJobParam').val('');
    $('#addJobHeader').val('');
    $('#addConcurrent').val('');
    $('#addTargetObject').val('');
    $('#addTargetMethod').val('');
    $('#addJobArguments').val('');
    $('#addExpiredDate').val('');
    $('#addCreateDate').val('');
    $('#addBeginDate').val('');
    $('#addExecStatus').val('');
    $('#addFailCallbackUrl').val('');
    $('#addRetryCount').val('');
    form.render();
  })

  // 编辑
  $('#edit').on('click', function() {
    var checkStatus = table.checkStatus('contentTable'); // 获取选中行
    var checkLength = checkStatus.data.length; // 获取选中行数量

    if(checkLength < 1) {
      layer.msg('请选择一条数据！');
    }else if(checkLength > 1) {
      layer.msg('只能选择一条数据！');
    }else {
      var checkData = checkStatus.data[0];
      layer.open({
        type: 1,
        title: '编辑',
        content: $('#editDialog'),
        area: ['720px', '510px'],
        btn: ['确定', '取消'],
        btnAlign: 'c',
        yes: function() {
          var parmas = getEditParmas();
          parmas.id = checkData.id;
//          parmas.updateUser = JSON.parse(window.sessionStorage.getItem("currentUser")).id;
          $.ajax({
            type: 'post',
            url: api +'/scheduleJob/update',
            data: parmas,
            dataType: 'json',
            success: function(data) {
//              if(data.code == 1) {
                layer.msg("修改成功！");
                renderTable();
//              }else {
//                layer.msg(data.msg);
//              }
            },
            error: function(data) {
              layer.msg(data.msg);
            }
          })
          layer.closeAll();
        }
      });

      laydate.render({
        elem: '#editBeginDate',
        type: 'datetime'
      })
      laydate.render({
        elem: '#editExpiredDate',
        type: 'datetime'
      })
      // 赋值
      $('#editId').val(checkData.id);
//      $('#editJobShowName').val(checkData.jobShowName);
      $('#editJobName').val(checkData.jobName);
      $('#editJobGroup').val(checkData.jobGroup);
      $('#editJobStatus').val(checkData.jobStatus);
      $('#editJobDesc').val(checkData.jobDesc);
      $('#editCronExpression').val(checkData.cronExpression);
      $('#editConcurrent').val(checkData.concurrent);
      $('#editTargetObject').val(checkData.targetObject);
      $('#editTargetMethod').val(checkData.targetMethod);
      $('#editJobUrl').val(checkData.jobUrl);
      $('#editJobParam').val(checkData.jobParam);
      $('#editJobHeader').val(checkData.jobHeader);
      $('#editExpiredDate').val(checkData.expiredDate);
      $('#editLinkType').val(checkData.linkType);
      $('#editLinkId').val(checkData.linkId);
      $('#editBeginDate').val(checkData.beginDate);
      $('#editExecStatus').val(checkData.execStatus);
      $('#editFailCallbackUrl').val(checkData.failCallbackUrl);
      $('#editRetryCount').val(checkData.retryCount);
      form.render();
    }
  })

  // 删除
  $('#delete').on('click', function() {
    var checkStatus = table.checkStatus('contentTable'); // 获取选中行
    var checkStatu = table.checkStatus('contentTable').data[0]; // 获取选中行
    var checkLength = checkStatus.data.length; // 获取选中行数量

    if(checkLength < 1) {
      layer.msg('请选择一条数据！');
    }else if(checkLength > 1) {
      layer.msg('只能选择一条数据！');
    }else {
      layer.open({
        type: 1,
        title: '删除',
        content: '<div style="font-size: 16px; padding: 30px 80px;">确定删除这条数据吗？</div>',
        maxWidth: null,
        btn: ['确定', '取消'],
        btnAlign: 'c',
        yes: function() {
          $.ajax({
            type: 'post',
            url: api +'/scheduleJob/delete',
            data: {
              id: checkStatu.id, 
//              updateUser: JSON.parse(window.sessionStorage.getItem("currentUser")).id
            },
            dataType: 'json',
            success: function(data) {
//              if(data.code == 1) {
                layer.msg("删除成功！");
                renderTable();
//              }else {
//                layer.msg(data.msg);
//              }
            },
            error: function(data) {
              layer.msg(data.msg);
            }
          })
          layer.closeAll();
        }
      });
    }
  })


  // 查询
  $('#search').on('click', function() {
    pageNo = 1;
    renderTable();
  })

  //监听工具条
  table.on('tool(jobTable)', function(obj){
    var data = obj.data;
    if(obj.event === 'dispose'){
      var obj = {
        id: data.id,
        execStatus: 1,
//        updateUser: JSON.parse(window.sessionStorage.getItem("currentUser")).id
      };
      handleFun(obj);
    }else if(obj.event === 'logCont') {
      checkLogDetail(data.id);
    }
  })

  function handleFun(obj) {
    $.ajax({
      type: 'post',
      url: api +'/scheduleJob/update',
      dataType: 'json',
      data: obj,
      async: false,
      success: function(data) {
        if(data.code == 1) {
          layer.msg(data.msg);
          renderTable();
        }else {
          layer.msg(data.msg);
        }
      },
      error: function(data) {
        layer.msg(data.msg);
      }
    })
  }

  function checkLogDetail(id) {
    layer.open({
      type: 1,
      title: id + ' 定时任务日志',
      content: $('#checkLogDialog'),
      area: ['800px', '500px'],
      btnAlign: 'c',
      success: function() {
        var logPageNo = 1, logPageSize = 10, logData = {}, logTableList = [], logCount = 0,
            logParmas = {
              jobId: id,
              pageNo: logPageNo,
              pageSize: logPageSize
            };
        logData = getLogDetail(logParmas);
        logCount = logData.rowCount;
        logTableList = logData.list;
        renderLogTable(logTableList, logPageSize);
        laypage.render({
          elem: 'logPage',
          count: logCount,
          curr: logPageNo,
          limit: logPageSize,
          layout: ['count', 'prev', 'page', 'next', 'limit', 'skip'],
          jump: function(obj, first){
            if(!first) {
              logPageNo = obj.curr,
              logPageSize = obj.limit;
              logParmas.pageNo = logPageNo;
              logParmas.pageSize = logPageSize;
              logData = getLogDetail(logParmas);
              logCount = logData.rowCount;
              logTableList = logData.list;
              renderLogTable(logTableList, logPageSize);
            }
          }
        });
      }
    });
  }

  function getLogDetail(logParmas) {
    var log_arr = [];
    $.ajax({
      type: 'get',
      url: api +'/scheduleJob/listByPageLog',
      dataType: 'json',
      data: logParmas,
      async: false,
      success: function(data) {
//        if(data.code == 1) {
          log_arr = data;
//        }else {
//          layer.msg(data.data);
//        }
      },
      error: function(data) {
        layer.msg(data.data);
      }
    })
    return log_arr;
  }

  function renderLogTable(logTableList, logPageSize) {
    logTableList.forEach(function(item, index) {
      switch(item.execStatus){
        case 1: item._execStatus = '正常'; break;
        case 2: item._execStatus = '错误'; break;
        case 3: item._execStatus = '异常'; break;
        default: item._execStatus = item.execStatus;
      }
      switch(item.optStatus){
        case 0: item._optStatus = '未操作'; break;
        case 1: item._optStatus = '已重试'; break;
        case 2: item._optStatus = '忽略'; break;
        default: item._optStatus = item.optStatus;
      }
    })
    table.render({
      elem: '#logTable',
      height: 380,
      data: logTableList,
      limit: logPageSize,
      cols: [[
        {field:'id', title: 'id', sort: true, fixed: 'left'},
        {field:'jobId', title: '定时任务Id', width: 100},
        {field:'execUrl', title: '执行的url', width: 180},
        {field:'execMsg', title: '执行返回信息', width: 130},
        {field:'_execStatus', title: '任务状态', width: 90},
        {field:'_optStatus', title: '操作状态', width: 90},
        {field:'createDate', title: '创建时间', width: 140},
        {field:'updateDate', title: '更新时间', width: 140},
      ]]
    });
  }

  function getAddParmas() {
    var obj = {};
    obj.id = $('#addId').val();
    obj.jobName = $('#addJobName').val();
    obj.jobStatus = $('#addJobStatus').val();
    obj.jobDesc = $('#addJobDesc').val();
    obj.cronExpression = $('#addCronExpression').val();
    obj.concurrent = $('#addConcurrent').val();
    obj.targetObject = $('#addTargetObject').val();
    obj.jobUrl = $('#addJobUrl').val();
    obj.jobParam = $('#addJobParam').val();
    obj.jobHeader = $('#addJobHeader').val();
    obj.targetMethod = $('#addTargetMethod').val();
    obj.expiredDate = $('#addExpiredDate').val();
    obj.createDate = $('#addCreateDate').val();
    obj.updateDate = $('#addUpdateDate').val();
    obj.beginDate = $('#addBeginDate').val();
    obj.execStatus = $('#addExecStatus').val();
    obj.failCallbackUrl = $('#addFailCallbackUrl').val();
    obj.retryCount = $('#addRetryCount').val();
    return obj;
  }

  function getEditParmas() {
    var obj = {};
    obj.id = $('#editId').val();
    obj.jobUrl = $('#editJobUrl').val();
    obj.jobName = $('#editJobName').val();
    obj.jobGroup = $('#editJobGroup').val();
    obj.jobStatus = $('#editJobStatus').val();
    obj.jobDesc = $('#editJobDesc').val();
    obj.cronExpression = $('#editCronExpression').val();
    obj.concurrent = $('#editConcurrent').val();
    obj.targetObject = $('#editTargetObject').val();
    obj.targetMethod = $('#editTargetMethod').val();
    obj.jobParam = $('#editJobParam').val();
    obj.expiredDate = $('#editExpiredDate').val();
    obj.jobHeader = $('#editJobHeader').val();
    obj.createDate = $('#editCreateDate').val();
    obj.updateDate = $('#editUpdateDate').val();
    obj.beginDate = $('#editBeginDate').val();
    obj.execStatus = $('#editExecStatus').val();
    obj.failCallbackUrl = $('#editFailCallbackUrl').val();
    obj.retryCount = $('#editRetryCount').val();
    return obj;
  }

  $('#upDown').on('click', function() {
    if($('.upIcon').css('display') == 'block') {
      $('.upIcon').hide();
      $('.downIcon').show();
      $('#handleContent').css({
        'display': 'none'
      });
      handleH = getHandleBoxHeight();
      renderTableData();
    }else {
      $('.downIcon').hide();
      $('.upIcon').show();
      $('#handleContent').css({
        'display': 'block'
      });
      handleH = getHandleBoxHeight();
      renderTableData();
    }
  })
})


