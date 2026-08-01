# 03 · 接口契约与数据字段

> 本文定义相册模块的接口与数据结构建议。路径/字段名允许在保持语义的前提下微调，但**响应包裹格式、错误处理策略、批处理返回结构**属于强约定。  
> 与历史描述冲突处，一律以 `05-change-log.md` 的 FINAL 为准。

## 1. 通用响应格式

新增的相册相关接口统一返回如下包裹结构：

```json
{ "code": 0, "message": "ok", "data": {} }
```

- 成功：`code = 0`，`message = "ok"`（或其它可读中文），业务数据放 `data`。
- 失败：`code` 为非 0 的业务错误码，`message` 为**可读中文**错误信息，`data` 可为 null。
- 禁止用"裸 500 / 空响应"表达业务失败；参数类错误应返回明确信息（如"相册不存在""图片不存在""封面必须是相册成员""相册名不能为空"等）。
- 现有的 `/api/upload`、`/api/list`、`/api/delete/{id}` 可保持其现状形态，不强制改造为该包裹格式。

## 2. 数据结构

### 2.1 Album（相册）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 主键 |
| name | string | 相册名，1~50 字符，去空白后非空 |
| description | string? | 简介，≤200 字符 |
| coverPictureId | number? | 手动封面 id，可空 |
| cover | object? | 计算后的展示封面（见 §4），含 id/url 等，供前端直接用 |
| pictureCount | number | 相册内图片数 |
| createTime | string | 创建时间 |
| updateTime | string | 最近更新时间 |

### 2.2 Membership（成员关系）

概念上为 `(albumId, pictureId, sortOrder, joinTime)`：

| 字段 | 说明 |
|------|------|
| albumId | 所属相册 |
| pictureId | 图片 id（引用现有 Picture，绝不复制图片记录） |
| sortOrder | 相册内排序位（越小越靠前；新加入者应获得最靠前的位置） |
| joinTime | 加入该相册的时间，用于"最近加入"口径 |

> 强调：成员关系是独立的多对多结构（关联表 / 关联实体），**不得**退化为 `Picture.albumId`。

## 3. 接口一览（建议）

### 阶段一
- `POST   /api/album` —— 新建相册，body: `{ name, description? }`。
- `GET    /api/album/list` —— 相册列表（含计算封面、数量、更新时间）。
- `GET    /api/album/{id}` —— 相册详情，返回相册信息 + 该相册内图片（按 sortOrder）。
- `PUT    /api/album/{id}` —— 重命名 / 改简介。
- `DELETE /api/album/{id}` —— 删除相册（仅删相册与成员关系，**不删图片**）。
- `POST   /api/album/{id}/pictures` —— 加入图片，body: `{ pictureId }`。
- `DELETE /api/album/{id}/pictures/{pictureId}` —— 移出单张（仅解关系）。

### 阶段二
- `PUT    /api/album/{id}/cover` —— 手动设封面，body: `{ pictureId }`；pictureId 必须是该相册成员，否则报错。
- `PUT    /api/album/{id}/order` —— 更新相册内顺序，body: `{ pictureIds: [...] }`（给出期望顺序）。

### 阶段三
- `POST   /api/album/{id}/pictures/batch` —— 批量加入，body: `{ pictureIds: [...] }`。
- `DELETE /api/album/{id}/pictures/batch` —— 批量移出，body: `{ pictureIds: [...] }`。

> 若实现者选择把批量移出用 POST + 动作字段表达，只要语义清晰亦可，但必须遵守下文 §5 的部分成功返回结构。

## 4. 计算封面（cover）返回口径

列表与详情返回的 `cover` 字段应为"最终展示封面"，其计算遵循 `02-requirements.md` 的 2.9：

1. 若 `coverPictureId` 非空且该图片仍是相册成员 → 用它。
2. 否则 → 用相册中最近加入（joinTime 最新）的成员。
3. 相册为空 → `cover = null`。

后端应保证：封面图片被移出或在图库删除后，再次请求列表/详情时 `cover` 已自动回退，无需前端处理。

## 5. 批处理返回结构（强约定，呼应 2.14 / HD 系列）

批量加入 / 批量移出采用**部分成功（partial success）**语义。即：有效项照常执行，无效项（图片不存在 / 不是成员 / 相册不存在等）单独汇报，**不因个别无效项导致整体回滚或静默丢弃**。

建议返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "requested": 5,
    "succeeded": [11, 12, 13],
    "ignored":   [14],
    "failed": [
      { "pictureId": 999, "reason": "图片不存在" }
    ]
  }
}
```

字段含义：
- `requested`：请求处理的条目数。
- `succeeded`：实际生效的 pictureId 列表（批量加入即新建的成员，批量移出即被解关系的成员）。
- `ignored`：因幂等被安全忽略的项（如批量加入时"已在相册中"的图片；批量移出时"本就不在相册中"的图片）。**忽略不算失败**。
- `failed`：真正无法处理的项，每项带 `pictureId` 与可读 `reason`。

特殊情形：
- 若请求中的**相册 id 本身不存在**，属于整单前置校验失败，返回 `code≠0`、`message="相册不存在"`，不进入部分成功流程。
- 若 `pictureIds` 为空数组，视为无操作，返回成功且 `succeeded/ignored/failed` 均为空。
- 批量加入时，`succeeded` 中的图片整体置顶；组内可按请求给定顺序排列（与 2.10 / 2.11 口径一致）。

> （DEPRECATED）早期 API 草案曾规定"批量接口只要有一个 id 非法就整单返回 400 且不做任何变更"。该"全有或全无"策略**已废弃**，被上述部分成功策略取代，详见 `05-change-log.md` CL-6。请勿实现全有或全无。
