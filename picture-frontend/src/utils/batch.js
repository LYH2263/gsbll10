export const summarizeBatchResult = (label, result) => {
  if (!result) return `${label}完成`
  const succeeded = result.succeeded ? result.succeeded.length : 0
  const ignored = result.ignored ? result.ignored.length : 0
  const failed = result.failed ? result.failed.length : 0
  const parts = [`成功 ${succeeded} 张`]
  if (ignored > 0) parts.push(`忽略 ${ignored} 张（重复）`)
  if (failed > 0) parts.push(`失败 ${failed} 张`)
  return `${label}：${parts.join('，')}`
}
