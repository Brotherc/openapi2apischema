<template>
  <div class="home">
    <a-layout class="layout">
      <a-layout-sider width="300" class="sider">
        <div class="select-container">
          <a-select
            v-model:value="selectedValue"
            class="select"
            placeholder="请选择"
            :options="options"
            :loading="loading"
            @change="handleSelectChange"
          />
        </div>
        <div class="list-container">
          <a-list
            :data-source="listData"
            :bordered="true"
            size="small"
            :loading="listLoading"
            hoverable
          >
            <template #renderItem="{ item }">
              <a-list-item @click="handleItemClick(item)">
                <div class="list-item-content">
                  <a-tooltip :title="item.subTitle">
                    <div class="list-item-title">
                      {{ item.title }}
                    </div>
                  </a-tooltip>
                  <div class="list-item-subtitle">{{ item.subTitle }}</div>
                </div>
              </a-list-item>
            </template>
          </a-list>
        </div>
      </a-layout-sider>
      <a-layout-content class="content">
        <div v-if="selectedApi" class="api-detail">
          <div class="api-title">{{ selectedApi.name }} <span class="api-subtitle">{{ selectedApi.cnName }}</span></div>
          <div class="api-desc">{{ selectedApi.description || '暂无描述' }}</div>

          <div class="table-title">请求参数</div>
          <a-table
            :columns="paramTableColumns"
            :data-source="selectedApi.displayParameters || []"
            :pagination="false"
            size="small"
            :defaultExpandAllRows="true"
            rowKey="name"
          />

          <div class="table-title">响应参数</div>
          <a-table
            :columns="paramTableColumns"
            :data-source="selectedApi.displayResponses || []"
            :pagination="false"
            size="small"
            :defaultExpandAllRows="true"
            rowKey="name"
          />
        </div>
        <div v-else class="empty-state">
          <a-spin :spinning="apiDetailLoading" tip="正在加载接口详情...">
            <a-empty v-if="!apiDetailLoading" description="请选择左侧接口查看详情" />
          </a-spin>
        </div>
      </a-layout-content>
    </a-layout>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

// Select组件数据
const selectedValue = ref(undefined)
const options = ref([])
const loading = ref(false)

// List组件数据
const listData = ref([])
const listLoading = ref(false)
const selectedApi = ref(null)
const apiDetailLoading = ref(false)

// 参数表格列定义
const paramTableColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    key: 'name',
    width: 280,
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    width: 100,
  },
  {
    title: '必填',
    dataIndex: 'required',
    key: 'required',
    width: 60,
    customRender: ({ text }) => text === true ? '是' : (text === false ? '否' : '')
  },
  {
    title: '示例值',
    dataIndex: 'example',
    key: 'example',
    width: 140,
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
  }
]

// 获取标签数据
const fetchTags = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/apiSchema/tags')
    options.value = response.data
    // 如果有数据，自动选中第一个选项
    if (options.value.length > 0) {
      selectedValue.value = options.value[0].value
      // 触发选择事件，加载对应的列表数据
      handleSelectChange(selectedValue.value)
    }
  } catch (error) {
    console.error('获取标签数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取API列表数据
const fetchApiList = async (tag) => {
  listLoading.value = true
  try {
    const response = await axios.get('/api/apiSchema/apiListByTag', {
      params: { tag }
    })
    listData.value = response.data
  } catch (error) {
    console.error('获取API列表失败:', error)
  } finally {
    listLoading.value = false
  }
}

// 处理Select选择变化
const handleSelectChange = (value) => {
  if (value) {
    fetchApiList(value)
  } else {
    listData.value = []
  }
  selectedApi.value = null
}

// 处理列表项点击
const handleItemClick = async (item) => {
  if (!selectedValue.value || !item.code) {
    console.warn('Tag或Code缺失，无法获取接口详情')
    selectedApi.value = null
    return
  }
  apiDetailLoading.value = true
  selectedApi.value = null // 清空之前的详情，避免显示旧数据导致类型错误
  try {
    const response = await axios.get('/api/apiSchema/apiAchemaByCode', {
      params: {
        tag: selectedValue.value,
        code: item.code
      }
    })
    const apiData = response.data;

    if (apiData) {
      // 确保 displayParameters 和 displayResponses 是数组，如果不是则使用空数组
      apiData.displayParameters = Array.isArray(apiData.displayParameters) ? apiData.displayParameters : [];
      apiData.displayResponses = Array.isArray(apiData.displayResponses) ? apiData.displayResponses : [];
      selectedApi.value = apiData;
    } else {
      console.error('获取接口详情失败: 返回数据为空或无效', apiData);
      selectedApi.value = null;
    }
  } catch (error) {
    console.error('获取接口详情失败:', error)
    selectedApi.value = null
  } finally {
    apiDetailLoading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchTags()
})
</script>

<style scoped>
.home {
  height: 100vh;
}

.layout {
  height: 100%;
}

.sider {
  background: #fff;
  padding: 8px 0;
  display: flex;
  flex-direction: column;
  min-width: 300px !important;
}

.select-container {
  margin-bottom: 8px;
  width: 100%;
  padding: 0 8px;
}

.select {
  width: 100% !important;
}

.list-container {
  flex: 1;
  overflow: auto;
  width: 100%;
}

.list-item-content {
  width: 100%;
  padding: 0 8px;
}

.list-item-title {
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: left;
  font-weight: 500;
}

.list-item-subtitle {
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: left;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  margin-top: 4px;
}

.content {
  padding: 16px;
  background: #fff;
  margin: 0;
  min-height: 280px;
  overflow: auto;
}

.api-detail {
  padding: 0 16px;
}

.api-title {
  font-size: 26px;
  font-weight: bold;
  margin-bottom: 4px;
  text-align: left;
}
.api-subtitle {
  font-size: 16px;
  color: #888;
  margin-left: 12px;
  text-align: left;
}
.api-desc {
  color: #666;
  margin-bottom: 18px;
  text-align: left;
}
.table-title {
  font-size: 18px;
  font-weight: bold;
  margin: 32px 0 8px 0;
  text-align: left;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.ant-list-item) {
  transition: background-color 0.3s;
}

:deep(.ant-list-item:hover) {
  background-color: #f5f5f5;
}
</style> 