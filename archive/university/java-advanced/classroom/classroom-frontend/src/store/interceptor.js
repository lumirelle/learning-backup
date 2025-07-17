import axios from 'axios'
import {Message} from "element-ui";
import router from '../router'

//定义拦截器
axios.interceptors.response.use((response)=>{
    if(response.status && response.status === 200 && response.data.status === 500) {
        Message.error({message: response.data.msg})
        return
    }
    if (response.data.msg) {
        Message.success({ message: response.data.msg, center: true })
    }
    return response;
},error => {
    if (error.response.status === 504 || error.response.status === 404) {
        Message.error({message: "没有找到服务，请确认请求地址是否正确"})
    } else if( error.response.status === 403) {
        Message.error({message: "权限不足"})
    } else if(error.response.status === 401){
        Message.error({message: "尚未登录，请登录"})
        router.replace("/")
    } else {
        Message.error({message: "未知错误"})
    }
})


let base = "";
//SpringSecurity默认不支持json登录,key,value登录
export const postKeyValueRequest = (url, params) => {
    return axios({
        method: 'post',
        url: `${base}${url}`,
        data: params,
        transformRequest: [function (data) {
            let ret = ''
            for (let i in data) {
                ret += encodeURIComponent(i) + '=' + encodeURIComponent(data[i]) + '&'
            }
            return ret
        }],
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
}