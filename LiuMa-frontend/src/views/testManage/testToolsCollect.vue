/**
* 测试通知
*/
<template>
  <div>
    <el-container>
      <el-header>
        <div>
          <el-radio-group v-model="testTools" size="medium">
            <el-radio-button label="Swagger对比"></el-radio-button>
            <el-radio-button label="待开发"></el-radio-button>
            <!-- <el-radio-button label="待开发"></el-radio-button>
                <el-radio-button  label="待开发"></el-radio-button> -->
          </el-radio-group>

        </div>


      </el-header>

    </el-container>
    <el-main>
      <div v-if="testTools == 'Swagger对比'">
        <p style="color: red;">TIPS:仅支持swagger3，生成的excel中请求体不好获取，需自行添加</p>

        <div style="display: flex;">


          <!-- 上传第一个Swagger JSON文件 -->
          <span>
            <el-upload ref="upload1" action="" :auto-upload="false" :on-change="file => handleBeforeUpload(file, 1)"
              :on-remove="() => handleRemove(1)" accept=".json" class="button">
              <el-button slot="trigger" size="small" type="primary">上传上一版swagger</el-button>
            </el-upload>
          </span>
          <!-- 上传第二个Swagger JSON文件 -->
          <span>
            <el-upload ref="upload2" action="" :auto-upload="false" :on-change="file => handleBeforeUpload(file, 2)"
              :on-remove="() => handleRemove(2)" accept=".json" class="button">
              <el-button slot="trigger" size="small" type="primary">上传最新swagger</el-button>
            </el-upload>

            <!-- 立即对比按钮 -->
            <el-button @click="compareFiles" type="success" size="small" style="float: right;">立即对比</el-button>
            <!-- 立即对比按钮 -->
            <el-button @click="compareSwaggerFiles" type="success" size="small"
              style="float: right;margin-right: 10px;">下载excel</el-button>
          </span>

        </div>
        <!-- 展示比较结果 -->
        <div v-if="diffResults">
          <div v-if="diffResults.added.length">
            <h3>新增的接口：</h3>
            <ul>
              <li v-for="item in diffResults.added" :key="item">
                {{ item }}
              </li>
            </ul>
          </div>
          <div v-if="diffResults.removed.length">
            <h3>删除的接口：</h3>
            <ul>
              <li v-for="item in diffResults.removed" :key="item">
                {{ item }}
              </li>
            </ul>
          </div>
          <div v-if="diffResults.modified.length">
            <h3>修改的接口：</h3>
            <ul>
              <li v-for="item in diffResults.modified" :key="item">
                {{ item }}
              </li>
            </ul>
          </div>
        </div>
      </div>

    </el-main>
  </div>
</template>

<script>
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

export default {
  created() {
    this.$root.Bus.$emit('initBread', ["测试工具", "测试工具集"]);
    this.currentUser = this.$store.state.userInfo.id;

  },
  data() {
    return {
      fileContents: [null, null], // 存储两个文件的内容
      diffResults: null, // 存储对比结果
      testTools: "Swagger对比"
    };
  },
  methods: {
    compareSwaggerFiles() {
      if (!this.fileContents[0] || !this.fileContents[1]) {
        this.$message.error('请先上传两个Swagger文件');
        return;
      }

      // 对比逻辑...
      const comparisonResult = this.performComparison();

      // 导出到Excel
      this.exportToExcel(comparisonResult);
    },
    performComparison() {
      // 这里应该是你的对比逻辑，返回对比结果数组
      // 示例结果，应根据实际逻辑生成
      const paths1 = this.fileContents[0].paths || {};
      const paths2 = this.fileContents[1].paths || {};

      const comparisonResult = [];

      // 找出增加和修改的接口
      Object.keys(paths2).forEach((path) => {
        Object.keys(paths2[path]).forEach((method) => {
          const endpoint2 = paths2[path][method];
          const tag = endpoint2.tags ? endpoint2.tags[0] : '未分类';
          const name = endpoint2.summary || '未命名';
          const requestBody = JSON.stringify(endpoint2.requestBody || {});

          if (!paths1[path] || !paths1[path][method]) {
            // 增加的接口
            comparisonResult.push({ tag, path, name, method: method.toUpperCase(), changeType: '增加', requestBody });
          } else {
            // 可能修改的接口
            const endpoint1 = paths1[path][method];
            const requestBody1 = JSON.stringify(endpoint1.requestBody || {});
            if (requestBody !== requestBody1) {
              comparisonResult.push({ tag, path, name, method: method.toUpperCase(), changeType: '修改', requestBody });
            }
          }
        });

      });
      return comparisonResult;
    },
    exportToExcel(data) {
      const ws = XLSX.utils.json_to_sheet(data, {
        header: ['changeType', 'tag', 'path', 'name', 'method', 'requestBody'],
      });
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, 'API Changes');
      const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'binary' });
      function s2ab(s) {
        const buf = new ArrayBuffer(s.length);
        const view = new Uint8Array(buf);
        for (let i = 0; i !== s.length; ++i) view[i] = s.charCodeAt(i) & 0xFF;
        return buf;
      }
      saveAs(new Blob([s2ab(wbout)], { type: 'application/octet-stream' }), 'API_Changes.xlsx');
    },
    handleBeforeUpload(file, index) {
      const reader = new FileReader();
      reader.onload = e => {
        this.fileContents[index - 1] = JSON.parse(e.target.result);
      };
      reader.readAsText(file.raw);
      return false; // 阻止自动上传
    },
    handleRemove(index) {
      this.fileContents[index - 1] = null;
      this.diffResults = null; // 清除比较结果
    },
    compareFiles() {
      console.log(this.fileContents)
      if (this.fileContents[0] && this.fileContents[1]) {
        this.diffResults = this.compareSwagger(this.fileContents[0].paths, this.fileContents[1].paths);
      } else {
        this.$message.error('请确保两个Swagger JSON文件都已上传！');
      }
    },
    compareSwagger(paths1, paths2) {
      let results = {
        added: [],
        removed: [],
        modified: []
      };

      // 检查新增和修改的接口
      for (const path in paths2) {
        if (!paths1.hasOwnProperty(path)) {
          results.added.push(path);
        } else if (JSON.stringify(paths1[path]) !== JSON.stringify(paths2[path])) {
          results.modified.push(path);
        }
      }

      // 检查删除的接口
      for (const path in paths1) {
        if (!paths2.hasOwnProperty(path)) {
          results.removed.push(path);
        }
      }

      return results;
    }
  },

};


</script>

<style scoped>
.checkbox-list {
  display: flex;
  flex-direction: column;
  float: right;
}

.button {
  display: inline-block;
  /* 或者使用 display: inline; */
  margin-right: 10px;
  /* 可选，为按钮添加一些间距 */
  float: left;
}
</style>
