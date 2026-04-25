<template>
  <div class="card" style="margin-bottom: 5px">
    <div class="search-bar">
      <el-input
        v-model="data.articleTitle"
        prefix-icon="Search"
        style="width: 240px; margin-right: 10px"
        placeholder="请输入帖子名称查询"
        clearable
        @clear="load"
        @keyup.enter="load"
      ></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div>
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div>
      <el-table
        stripe
        :data="data.tableData"
        style="width: 100%; margin-top: 20px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="img" label="展示图" width="200">
          <template v-slot="scope">
            <el-image
              style="width: 100px; height: 50px; border-radius: 5px; display: block; cursor: pointer"
              v-if="scope.row.img"
              :src="scope.row.img"
              :preview-src-list="[scope.row.img]"
              preview-teleported
              fit="cover"
              @click="goToDetail(scope.row)"
            ></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="targetTitle" label="标题" align="center" />
        <el-table-column label="操作" width="240" align="center">
          <template v-slot="scope">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)"></el-button>
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="data.pageNum"
        v-model:page-size="data.pageSize"
        :total="data.total"
        layout="total, prev, pager, next, jumper"
        @size-change="load"
        @current-change="load"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog title="轮播图信息" v-model="data.formVisible" width="50%" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px" style="padding: 20px">

        <!-- 图片上传组件 -->
        <el-form-item prop="img" label="图片">
          <el-upload
            :action="baseUrl + '/files/upload'"
            :on-success="handleImgUpload"
            :on-remove="handleRemove"
            :file-list="data.fileList"
            list-type="picture"
          >
            <el-button type="primary">
              {{ data.form.img ? '重新上传' : '点击上传' }}
            </el-button>
          </el-upload>
        </el-form-item>

        <!-- 选择链接类型 -->
        <el-form-item prop="targetType" label="链接类型" required>
          <el-radio-group v-model="data.form.targetType">
            <el-radio value="case">经典案例</el-radio>
            <el-radio value="article">论坛帖子</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 选择帖子 -->
        <el-form-item prop="targetId" label="选择帖子" v-if="data.form.targetType === 'article'">
          <el-select v-model="data.form.targetId" placeholder="请选择帖子">
            <el-option v-for="item in data.articleData" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>

        <!-- 选择案例 -->
        <el-form-item prop="targetId" label="选择案例" v-if="data.form.targetType === 'case'">
          <el-select v-model="data.form.targetId" placeholder="请选择案例">
            <el-option v-for="item in data.caseData" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit } from "@element-plus/icons-vue";

const formRef = ref();
const baseUrl = import.meta.env.VITE_BASE_URL;

const data = reactive({
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  articleTitle: null,
  ids: [],
  articleData: [],
  caseData: [],
  fileList: [],
  rules: {
    img: [{ required: true, message: '请上传轮播图图片', trigger: 'blur' }],
    targetId: [{ required: true, message: '请选择内容', trigger: 'change' }],
  }
});

// 加载帖子列表
const loadArticle = () => {
  request.get('/article/selectAll', {
    params: {
      status: '审核通过'
    }
  }).then(res => {
    if (res.code === '200') {
      data.articleData = res.data;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 加载案例列表
const loadCase = () => {
  request.get('/case/selectAll').then(res => {
    if (res.code === '200') {
      data.caseData = res.data || [];
    }
  });
};

// 加载轮播图列表
const load = () => {
  request.get('/carousel/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      targetTitle: data.articleTitle
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || [];
      data.total = res.data?.total;
    }
  });
};

// 新增
const handleAdd = () => {
  data.form = {};
  data.fileList = [];  // 👈 修复：清空文件列表
  data.formVisible = true;
};

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  if (row.img) {
    data.fileList = [{ url: row.img }];
  } else {
    data.fileList = [];
  }
  data.formVisible = true;
  console.log(data.form);
};

// 添加
const add = () => {
  request.post('/carousel/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功');
      data.formVisible = false;
      load();
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 更新
const update = () => {
  request.put('/carousel/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功');
      data.formVisible = false;
      load();
    }
  });
};

// 保存
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? update() : add();
    }
  });
};

// 删除单个
const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' })
    .then(() => {
      request.delete('/carousel/delete/' + id).then(res => {
        if (res.code === '200') {
          ElMessage.success("删除成功");
          load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    })
    .catch(err => {
      console.error(err);
    });
};

// 批量删除
const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据");
    return;
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' })
    .then(() => {
      request.delete("/carousel/delete/batch", { data: data.ids }).then(res => {
        if (res.code === '200') {
          ElMessage.success('操作成功');
          load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    })
    .catch(err => {
      console.error(err);
    });
};

// 选择变化
const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id);
};

// 重置
const reset = () => {
  data.articleTitle = null;
  data.fileList = [];  // 👈 修复：改为空数组
  data.pageNum = 1;    // 👈 修复：加上 data.
  load();
};

// 图片上传成功
const handleImgUpload = (res) => {
  data.form.img = res.data;
  data.fileList = [{ url: res.data }];  // 👈 修复：更新 fileList
};

// 👇 添加：删除图片
const handleRemove = () => {
  data.form.img = null;
  data.fileList = [];
};

// 点击图片跳转详情
const goToDetail = (row) => {
  if (!row.targetId) {
    ElMessage.warning('暂无关联内容');
    return;
  }

  if (row.targetType === 'article') {
    window.open(`/front/articleDetail?id=${row.targetId}`, '_blank');
  } else if (row.targetType === 'case') {
    window.open(`/front/caseDetail?id=${row.targetId}`, '_blank');
  }
};

// 初始化加载
loadArticle();
loadCase();
load();
</script>

<style scoped>
.card {
  background: white;
  padding: 10px 20px;
  border-radius: 8px;
}

.search-bar {
  background: white;
  padding: 10px 10px 15px 0px;
  margin-bottom: 5px;
  border-radius: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.pagination {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  background: white;
  padding: 20px;
  border-radius: 8px;
}
</style>