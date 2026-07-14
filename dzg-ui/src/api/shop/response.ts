export const optionList = <T>(response: any): T[] => {
  if (Array.isArray(response?.data)) {
    return response.data;
  }
  if (Array.isArray(response?.rows)) {
    return response.rows;
  }
  if (Array.isArray(response?.data?.rows)) {
    return response.data.rows;
  }
  if (Array.isArray(response?.data?.records)) {
    return response.data.records;
  }
  return [];
};
