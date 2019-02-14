$(document).ready(
  function() { 
		// $("#navBar").niceScroll();
		// $("#navBar").getNiceScroll().hide();     // 隐藏滚动条
    // $("#navBar").getNiceScroll().resize();   // 当窗口改变大小时，滚动条重置大小
    // 控制左菜单栏的显示隐藏
    $("#showHide").on("click",function(){
      if($(".leftFlex").css('display')=='block') {
				$('.layui-layout-admin .layui-side').css('left', -220);
        $(".layui-body").css("left",20);
        $(".leftFlex").hide();
        $(".rightSpread").show();
      }
      else {
				$('.layui-layout-admin .layui-side').css('left', 0);
        $(".layui-body").css("left", 240);
        $(".leftFlex").show();
        $(".rightSpread").hide();
      }
		})
		// 左侧导航菜单和历史菜单切换
		$('#sidebar-collapse').on('click', 'p', function() {
			var ind = $(this).index();
			$(this).addClass('curMenuTit').siblings().removeClass('curMenuTit');
			$('#navContent').find('div.nav').hide().eq(ind).show();
		})
  }
);

var $,tab,skyconsWeather;
layui.config({
	base : "js/"
}).use(['bodyTab','form','element','layer','jquery'],function(){
	var form = layui.form,
			layer = layui.layer,
			element = layui.element;
			$ = layui.jquery;
			tab = layui.bodyTab();
			
	// 当前用户 赋值
	var currentUser = JSON.parse(sessionStorage.getItem("currentUser"));
	$('#loginUser').html(currentUser.userName);
	$('#loginUserL').html(currentUser.userName);
	$('#lockUserName').html(currentUser.userName);
	// 退出
	$('#dropOut').on('click', function() {
		window.location.href="./page/login.html";
	})

	//手机设备的简单适配
	var treeMobile = $('.site-tree-mobile'),
		shadeMobile = $('.site-mobile-shade')

	treeMobile.on('click', function(){
		$('body').addClass('site-mobile');
	});

	shadeMobile.on('click', function(){
		$('body').removeClass('site-mobile');
	});

  // 添加新窗口
  var liHeight, liLength1, liLength2, allHeight, maxHeight1, maxHeight2;
	//获取头部的高度
	getMenuItemHeight();
  function getMenuItemHeight() {
		liHeight = $('.layui-nav-tree>.layui-nav-item>a').height();
		liLength1 = $('.navBar .layui-nav-tree .layui-nav-item').length;
		liLength2 = $('.historyNav .layui-nav-tree .layui-nav-item').length;
		allHeight = $('#navContent').height();
		maxHeight1 = allHeight-liLength1*liHeight;
		maxHeight2 = allHeight-liLength2*liHeight;
		$(".navBar .layui-nav li:first-child .layui-nav-child").height(maxHeight1);
		$(".historyNav .layui-nav li:first-child .layui-nav-child").height(maxHeight2);
	}
  //随窗口变化重新获取maxHeight的高度
  $(window).resize(function() {
    getMenuItemHeight();
  })
	

	$(".navBar .layui-nav .layui-nav-item a").on("click",function(){
		if($(this)[0].parentNode.tagName == 'LI') {
    	$(this).parent("li").addClass("showItem").siblings().removeClass("showItem");
			$(this).siblings(".layui-nav-child").height(maxHeight1);
		}else {
    	addTab($(this));
		}
	})

	//公告层
	function showNotice(){
		layer.open({
			type: 1,
			title: "系统公告", //不显示标题栏
			closeBtn: false,
			area: '310px',
			shade: 0.8,
			id: 'LAY_layuipro', //设定一个id，防止重复弹出
			btn: ['火速围观'],
			moveType: 1, //拖拽模式，0或者1
			content: '<div style="padding:15px 20px; text-align:justify; line-height: 22px; text-indent:2em;border-bottom:1px solid #e2e2e2;"><p>最近偶然发现贤心大神的layui框架，瞬间被他的完美样式所吸引，虽然功能不算强大，但毕竟是一个刚刚出现的框架，后面会慢慢完善的。很早之前就想做一套后台模版，但是感觉bootstrop代码的冗余太大，不是非常喜欢，自己写又太累，所以一直闲置了下来。直到遇到了layui我才又燃起了制作一套后台模版的斗志。由于本人只是纯前端，所以页面只是单纯的实现了效果，没有做服务器端的一些处理，可能后期技术跟上了会更新的，如果有什么问题欢迎大家指导。谢谢大家。</p><p>在此特别感谢Beginner和Paco，他们写的框架给了我很好的启发和借鉴。希望有时间可以多多请教。</p></div>',
			success: function(layero){
				var btn = layero.find('.layui-layer-btn');
				btn.css('text-align', 'center');
				btn.on("click",function(){
					window.sessionStorage.setItem("showNotice","true");
				})
				if($(window).width() > 432){  //如果页面宽度不足以显示顶部“系统公告”按钮，则不提示
					btn.on("click",function(){
						layer.tips('系统公告躲在了这里', '#showNotice', {
							tips: 3
						});
					})
				}
			}
		});
  }
  
})

//打开新窗口
function addTab(_this){
	tab.tabAdd(_this);
}