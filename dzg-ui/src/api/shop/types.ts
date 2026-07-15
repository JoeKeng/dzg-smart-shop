export interface ShopCategory {
  categoryId?: string | number;
  categoryName?: string;
  sortOrder?: number;
  status?: string;
  remark?: string;
}

export interface ShopProduct {
  productId?: string | number;
  categoryId?: string | number;
  categoryName?: string;
  productName?: string;
  barcode?: string;
  unitName?: string;
  salePrice?: number;
  purchasePrice?: number;
  warningQty?: number;
  imageUrl?: string;
  imageOssId?: string;
  supplierId?: string | number;
  supplierIds?: Array<string | number>;
  supplierNames?: string;
  status?: string;
  remark?: string;
}

export interface ShopStock {
  stockId?: string | number;
  productId?: string | number;
  quantity?: number;
  warningQty?: number;
}

export interface ShopCustomer {
  customerId?: string | number;
  customerName?: string;
  phone?: string;
  creditLimit?: number;
  currentDebt?: number;
  status?: string;
  remark?: string;
}

export interface ShopOrderItem {
  productId?: string | number;
  quantity?: number;
}

export interface ShopOrderForm {
  customerId?: string | number;
  payType?: string;
  paidAmount?: number;
  remark?: string;
  items: ShopOrderItem[];
}

export interface ShopCreditRecord {
  creditId?: string | number;
  orderId?: string | number;
  customerId?: string | number;
  creditAmount?: number;
  paidAmount?: number;
  unpaidAmount?: number;
  status?: string;
  remark?: string;
}

export interface ShopDashboard {
  todaySales: number;
  todayOrderCount: number;
  todayCredit: number;
  unpaidTotal: number;
  lowStockCount: number;
}

export interface ShopSupplier {
  supplierId?: string | number;
  supplierName?: string;
  contactName?: string;
  phone?: string;
  address?: string;
  productCount?: number;
  status?: string;
  remark?: string;
}

export interface ShopPurchaseItem {
  productId?: string | number;
  quantity?: number;
  purchasePrice?: number;
}

export interface ShopPurchaseForm {
  supplierId?: string | number;
  remark?: string;
  items: ShopPurchaseItem[];
}

export interface ShopPurchaseOrder {
  purchaseId?: string | number;
  purchaseNo?: string;
  supplierId?: string | number;
  totalAmount?: number;
  status?: string;
  purchaseTime?: string;
  remark?: string;
}

export interface ShopReport {
  salesTotal: number;
  orderCount: number;
  creditTotal: number;
  unpaidTotal: number;
  grossProfit: number;
  lowStockCount: number;
}

export interface ShopNotification {
  noticeId?: string | number;
  noticeType?: string;
  noticeTitle?: string;
  noticeContent?: string;
  bizType?: string;
  bizId?: string | number;
  status?: string;
}

export interface ShopAiInsight {
  type?: string;
  title?: string;
  content?: string;
  level?: 'success' | 'info' | 'warning' | 'danger' | string;
  actionText?: string;
  actionPath?: string;
}

export interface ShopAiAnalysis {
  summary?: string;
  riskLevel?: 'low' | 'medium' | 'high' | string;
  insights?: ShopAiInsight[];
}
