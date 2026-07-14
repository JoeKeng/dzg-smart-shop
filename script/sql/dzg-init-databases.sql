-- 店掌柜智慧零售管理系统数据库初始化脚本
-- 先执行本脚本创建数据库，再导入对应业务脚本。

CREATE DATABASE IF NOT EXISTS dzg_cloud
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS dzg_job
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS dzg_workflow
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS dzg_seata
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS dzg_config
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
