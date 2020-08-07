$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			CONTEXT_PATH + "/follow",
			{"entityType":3, "entityId":$(btn).prev().val()},
			function (data) {
				data = $.parseJSON(data);
				if(data.code == 200) {
					// 这里这是简单刷新
					// 可以局部刷新，更新关注人数，提示已关注
					window.location.reload();
				} else {
					alert(data.msg);
				}
			}
		)
		// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$.post(
			CONTEXT_PATH + "/unfollow",
			{"entityType":3, "entityId":$(btn).prev().val()},
			function (data) {
				data = $.parseJSON(data);
				if(data.code == 200) {
					// 这里这是简单刷新
					// 可以局部刷新，更新关注人数，提示取消关注
					window.location.reload();
				} else {
					alert(data.msg);
				}
			}
		)
		// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}