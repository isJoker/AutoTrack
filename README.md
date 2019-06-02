# AutoTrack
页面浏览事件全埋点

## 收集页面浏览事件json如下

  ### 打开首页
   {
    	"event":"AppViewScreen",
    	"time":"2019-06-02 13:31:24.403",
    	"device_id":"2e570fc4471912a7",
    	"properties":{
    		"app_name":"AutoTrack",
    		"screen_width":1080,
    		"screen_height":1794,
    		"lib":"Android",
    		"os":"Android",
    		"app_version":"1.0",
    		"os_version":"8.0.0",
    		"model":"Android SDK built for x86",
    		"lib_version":"1.0.0",
    		"manufacturer":"Google",
    		"activity":"cn.com.autotrack.MainActivity",
    		"title":"首页"
    	}
    }
    
    ### 点击首页button进入第二个页面
    
    {
    	"event":"AppViewScreen",
    	"time":"2019-06-02 13:35:57.383",
    	"device_id":"2e570fc4471912a7",
    	"properties":{
    		"app_name":"AutoTrack",
    		"screen_width":1080,
    		"screen_height":1794,
    		"lib":"Android",
    		"os":"Android",
    		"app_version":"1.0",
    		"os_version":"8.0.0",
    		"model":"Android SDK built for x86",
    		"lib_version":"1.0.0",
    		"manufacturer":"Google",
    		"activity":"cn.com.autotrack.SecondActivity",
    		"title":"第二个页面"
    	}
    }
