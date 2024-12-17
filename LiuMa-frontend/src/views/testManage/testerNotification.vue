/**
 * 测试通知
 */
<template>
  <div>
    <el-container>
        <el-header>
            <div>
            <el-button
                size="mini"
                @click="createElement()">新增
            </el-button>
            <el-button
                size="mini"
                @click="generate()">手动生成通知
            </el-button>

            </div>

        </el-header>

    </el-container>
    <el-main>
        <el-table
        :data="tableData"
        style="width: 100%"
        v-loading="loading" element-loading-text="拼命加载中"
        >
        <el-table-column
        label="id"
        width="180">
        <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.id }}</span>
        </template>
        </el-table-column>
        <el-table-column
        label="通知顺序"
        width="280">
        <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.ranker }}</span>
        </template>
        </el-table-column>
        <el-table-column
        label="执行时间"
        width="180">
        <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.runTime | timeFilter}}</span>
        </template>
        </el-table-column>
        <el-table-column
        label="执行频率"
        width="180">
        <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.frequency | tagsStr}}</span>
        </template>
        </el-table-column>
        <el-table-column
        label="通知状态"
        width="100">
        <template slot-scope="scope">
            <span style="margin-left: 10px" :style="activation(scope.row.status)">{{ scope.row.status | statusFilter}}</span>
        </template>
        </el-table-column>
        <el-table-column label="操作">
        <template slot-scope="scope">
            <el-button
            size="mini"
            @click="editElement(scope.row)">编辑
            </el-button>
            <el-button
            size="mini"
            @click="deleteElement(scope.row)">删除
            </el-button>
            <el-button
            size="mini"
            @click="execJob(scope.row)">{{scope.row.status==0?'启动':'暂停'}}
            </el-button>
            <!-- <el-button
            size="mini"
            @click="generate(scope.row)">手动生成通知
            </el-button> -->
        </template>
        </el-table-column>
    </el-table>

        <!-- 生成内容弹窗 -->
        <el-dialog title="生成通知"  :visible.sync="generateVisible" width="40%" autosize destroy-on-close @close="closeGenerate">
            <el-input type="textarea" autosize v-model="generateContent" id="generateText" v-loading="noticeLoading" element-loading-text="拼命生成中"></el-input>
            <div slot="footer" class="dialog-footer">
                <span style="color: red;float:left;">Tips: 点关闭按钮x, 即可复制通知</span>
            </div>
        </el-dialog>

    <!-- 编辑顺序弹框 -->
    <el-dialog title="编辑通知顺序" :visible.sync="elementVisible" width="40%" destroy-on-close >
            <el-form label-width="120px" style="padding-right: 30px;" :model="addElementForm"  ref="addElementForm">
                <el-form-item label="id" prop="id">
                    <el-input size="small" style="width:95%" v-model="addElementForm.id" auto-complete="off" placeholder="元素名称" :disabled="true"/>
                </el-form-item>
                <el-form-item label="通知顺序" prop="ranker" required>
                    <el-input size="small" style="width:95%" v-model="addElementForm.ranker" auto-complete="off" placeholder="通知顺序" required/>
                </el-form-item>
                <el-form-item label="执行时间"  prop="runTime" required>
                    <el-time-picker
                        style="width:95%"
                        v-model="addElementForm.runTime"
                        format="HH:mm"
                        placeholder="任意时间点">
                    </el-time-picker>
                </el-form-item>
                <el-form-item label="执行频率" prop="frequency" style="width:95%" required>
                    <el-select v-model="addElementForm.frequency"  placeholder="请选择" style="width:95%">
                    <el-option
                        v-for="item in frequencyList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                </el-select>

                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <span style="color: red;float:left;">姓名与姓名之间用管道符号 | 连接</span>
                <el-button size="small" @click="elementVisible=false">取消</el-button>
                <el-button size="small" type="primary" @click="submitElement('addElementForm')">确定</el-button>
            </div>
        </el-dialog>
    </el-main>
  </div>
</template>

<script>
import Pagination from '../common/components/pagination'

import moment from 'moment'
import { del } from 'vue';

export default {
    components: {
        Pagination
    },
    filters:{
        timeFilter (val) {
            return moment(val).format('HH:mm');
        },
        statusFilter(val){
            return val=="0"?"已暂停":"已启动";
        },
        tagsStr(val) {
            let a=""
            for (let i = 0; i < val.length; i++) {
                if(val[i]=="1"){
                    a=a+"星期一 "
                }
                if(val[i]=="2"){
                    a=a+"星期二 "
                }
                if(val[i]=="3"){
                    a=a+"星期三 "
                }
                if(val[i]=="4"){
                    a=a+"星期四 "
                }
                if(val[i]=="5"){
                    a=a+"星期五 "
                }
                if(val[i]=="6"){
                    a=a+"星期六 "
                }
                if(val[i]=="7"){
                    a=a+"星期日 "
                }
            }
            return a;
        }
    },
    data() {
        return{
            generateContent:"",
            date1:"",
            tableData: [],
            generateVisible:false,
            elementVisible: false,
            addElementForm: {
                id:"",
                ranker:"",
                runTime:"",
                frequency:"",
                deleted:"",
            },
            loading:false,
            noticeLoading:false,
            frequencyList: [
                {value: "1", label: "星期一"},
                {value: "2", label: "星期二"},
                {value: "3", label: "星期三"},
                {value: "4", label: "星期四"},
                {value: "5", label: "星期五"},
                {value: "6", label: "星期六"},
                {value: "7", label: "星期日"},
            ]
        }
    },
    computed: {
        activation(){
            return (status)=>{
                if(status=="0"){
                    return {color: "red"}
                }else if(status=="1"){
                    return {color: "green"}
                }
            }
        }

  },
    created() {
        this.$root.Bus.$emit('initBread', ["测试工具", "测试者通知"]);
        this.currentUser = this.$store.state.userInfo.id;
        this.getdata();
    },

    methods: {
        timestampToTime(timestamp){
            return new Date(timestamp*1000);
        },
        // timestampToTime(timestamp){
        //     // 将时间戳转换为 Date 对象
        //     const date = new Date(timestamp)

        //     // 获取年月日时分秒信息
        //     const year = date.getFullYear()
        //     const month = date.getMonth() + 1
        //     const day = date.getDate()
        //     const hour = date.getHours()
        //     const minute = date.getMinutes()
        //     const second = date.getSeconds()

        //     // 根据需求进行格式化输出
        //     const timeStr = `${year}-${month}-${day} ${hour}:${minute}:${second}`
        //     return timeStr;

        // },


        // 编辑元素
        editElement(row){
            console.log(row);
            this.addElementForm=row
            this.elementVisible = true;
        },
        // 新增元素
        createElement(){
            for(let a in this.addElementForm){
                delete this.addElementForm[a];
            }
            this.elementVisible = true;
        },
        handleEdit(index, row) {
            console.log(index, row);
        },
        handleDelete(index, row) {
            console.log(index, row);
        },
        // 获取列表数据方法
        getdata() {
            this.loading = true;
            let url = '/autotest/order/getall';
            this.$get(url, response => {
                let data = response.data;
                for(let i=0;i<data.length;i++){
                    data[i].runTime=new Date(data[i].runTime*1000);
                }
                this.tableData=data;

                console.log(data);
                this.loading = false;
            });
        },
        submitElement(formName) {
            if(typeof  this.addElementForm.id === 'undefined' || this.addElementForm.id === null ){
                console.log(this.addElementForm);
                this.$refs[formName].validate(valid => {
                if(valid){
                    this.loading = true;
                    let url = '/autotest/order/create';
                    let dateTime= this.addElementForm.runTime;
                    var timestamp1 = Date.parse(dateTime)/1000;
                    this.addElementForm.runTime=timestamp1;
                    this.$post(url,this.addElementForm, response => {
                        let data = response.data;
                        this.getdata();
                        this.elementVisible = false;
                        this.loading = false;
                        this.$message({
                            message: '保存成功！',
                            type: 'success'
                        });
                        })
                        }})

            }else{
            this.$refs[formName].validate(valid => {
                if(valid){
                    this.loading = true;
                    let url = '/autotest/order/update';
                    let dateTime= this.addElementForm.runTime;
                    var timestamp1 = Date.parse(dateTime)/1000;
                    this.addElementForm.runTime=timestamp1;
                    this.addElementForm.frequency=this.addElementForm.frequency;
                    this.addElementForm.deleted="0";
                    let param = this.addElementForm;
                    console.log(param);
                    this.$post(url,param, response => {
                        let data = response.data;
                        this.getdata();
                        this.elementVisible = false;
                        this.loading = false;
                        this.$message({
                            message: '保存成功！',
                            type: 'success'
                        });
                    });
                }else{
                    return false
                }
                })}

        },
        // 获取列表数据方法
        generate() {
            this.generateVisible = true;
            this.noticeLoading = true;
            let url = '/autotest/order/generate';
            this.$get(url, response => {
                this.generateContent = response.data;
                this.noticeLoading = false;
                // this.generateVisible = false;
            });

            this.loading=false;
        },

        closeGenerate(){
            let getGenerateData = document.getElementById("generateText").value;
            console.log(getGenerateData);
            navigator.clipboard.writeText(getGenerateData);
        },
        deleteElement(row){
            this.loading=true;
            let id =row.id
            let url = '/autotest/order/delete?id='+id;
            this.$get(url, response => {
                this.generateContent = response.data;
                this.getdata();
                this.loading=false;
                // this.generateVisible = false;
            });
        },

        execJob(row){
            this.loading=true;
            let status =row.status;
            this.addElementForm.status= (status=="1"?"0":"1");
            this.addElementForm.deleted="0";
            this.addElementForm.id=row.id;
            this.addElementForm.jobId=row.jobId;
            let param = this.addElementForm;
            console.log(param);
            let url = '/autotest/order/control';
            this.$post(url,param, response => {
                        let data = response.data;
                        this.getdata();
                        this.elementVisible = false;
                        this.loading = false;
                        this.$message({
                            message: '保存成功！',
                            type: 'success'
                        });});
        },



    }
}
</script>

<style scoped>

</style>
