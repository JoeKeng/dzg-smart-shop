<template>
  <div class="login">
    <div class="login-scene" aria-hidden="true">
      <div class="pixel-cloud cloud-a"></div>
      <div class="pixel-cloud cloud-b"></div>
      <div class="pixel-house"></div>
      <div class="pixel-fence"></div>
    </div>

    <section class="login-shell" aria-label="店掌柜登录">
      <aside class="login-sidebar" aria-hidden="true">
        <div class="wood-sign">
          <span class="sign-icon">柜</span>
          <strong>店掌柜</strong>
        </div>
        <div class="side-card">
          <h2>小店经营台</h2>
          <p>收银、库存、赊账、提醒都在一个工作台。</p>
        </div>
        <div class="pixel-crops">
          <i></i><i></i><i></i><i></i><i></i>
        </div>
      </aside>

      <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form" aria-label="登录表单" @submit.prevent="handleLogin">
      <div class="title-box">
        <p class="eyebrow">Dian Zhang Gui</p>
        <h1 class="title">欢迎回来</h1>
        <p class="subtitle">登录后继续管理今天的小店生意</p>
      </div>
      <el-form-item v-if="tenantEnabled" prop="tenantId">
        <el-select v-model="loginForm.tenantId" filterable :placeholder="proxy.$t('login.selectPlaceholder')" aria-label="租户" style="width: 100%">
          <el-option v-for="item in tenantList" :key="item.tenantId" :label="item.companyName" :value="item.tenantId"></el-option>
          <template #prefix><svg-icon icon-class="company" class="el-input__icon input-icon" /></template>
        </el-select>
      </el-form-item>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          name="username"
          size="large"
          autocomplete="username"
          aria-label="用户名"
          spellcheck="false"
          :placeholder="proxy.$t('login.username')"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          name="password"
          size="large"
          autocomplete="current-password"
          aria-label="密码"
          :placeholder="proxy.$t('login.password')"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="captchaEnabled" prop="code">
        <el-input
          v-model="loginForm.code"
          name="captcha"
          size="large"
          autocomplete="one-time-code"
          aria-label="验证码"
          spellcheck="false"
          :placeholder="proxy.$t('login.code')"
          style="width: 63%"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <button class="login-code" type="button" aria-label="刷新验证码" title="刷新验证码" @click="getCode">
          <img :src="codeUrl" class="login-code-img" width="160" height="40" alt="验证码" />
        </button>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" class="remember-check">{{ proxy.$t('login.rememberPassword') }}</el-checkbox>
      <el-form-item style="width: 100%">
        <el-button class="login-submit" :loading="loading" :aria-busy="loading" native-type="submit" size="large" type="primary">
          <span v-if="!loading">{{ proxy.$t('login.login') }}</span>
          <span v-else>{{ proxy.$t('login.logging') }}</span>
        </el-button>
        <div v-if="register" style="float: right">
          <router-link class="link-type" :to="'/register'">{{ proxy.$t('login.switchRegisterPage') }}</router-link>
        </div>
      </el-form-item>
      </el-form>
    </section>
    <div class="el-login-footer">
      <span>店掌柜智慧零售管理系统</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getCodeImg, getTenantList } from '@/api/login';
import { useUserStore } from '@/store/modules/user';
import { LoginData, TenantVO } from '@/api/types';
import { to } from 'await-to-js';
import { useI18n } from 'vue-i18n';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const userStore = useUserStore();
const router = useRouter();
const { t } = useI18n();

const loginForm = ref<LoginData>({
  tenantId: '000000',
  username: 'admin',
  password: 'admin123',
  rememberMe: false,
  code: '',
  uuid: ''
} as LoginData);

const loginRules: ElFormRules = {
  tenantId: [{ required: true, trigger: 'blur', message: t('login.rule.tenantId.required') }],
  username: [{ required: true, trigger: 'blur', message: t('login.rule.username.required') }],
  password: [{ required: true, trigger: 'blur', message: t('login.rule.password.required') }],
  code: [{ required: true, trigger: 'change', message: t('login.rule.code.required') }]
};

const codeUrl = ref('');
const loading = ref(false);
// 验证码开关
const captchaEnabled = ref(true);
// 店掌柜默认使用固定租户，登录页不展示租户选择。
const tenantEnabled = ref(false);

// 注册开关
const register = ref(false);
const redirect = ref('/');
const loginRef = ref<ElFormInstance>();
// 租户列表
const tenantList = ref<TenantVO[]>([]);

watch(
  () => router.currentRoute.value,
  (newRoute: any) => {
    redirect.value = newRoute.query && newRoute.query.redirect && decodeURIComponent(newRoute.query.redirect);
  },
  { immediate: true }
);

const handleLogin = () => {
  loginRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true;
      // 勾选了需要记住密码设置在 localStorage 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        localStorage.setItem('tenantId', String(loginForm.value.tenantId));
        localStorage.setItem('username', String(loginForm.value.username));
        localStorage.setItem('password', String(loginForm.value.password));
        localStorage.setItem('rememberMe', String(loginForm.value.rememberMe));
      } else {
        // 否则移除
        localStorage.removeItem('tenantId');
        localStorage.removeItem('username');
        localStorage.removeItem('password');
        localStorage.removeItem('rememberMe');
      }
      // 调用action的登录方法
      const [err] = await to(userStore.login(loginForm.value));
      if (!err) {
        const redirectUrl = redirect.value || '/';
        await router.push(redirectUrl);
        loading.value = false;
      } else {
        loading.value = false;
        // 重新获取验证码
        if (captchaEnabled.value) {
          await getCode();
        }
      }
    }
  });
};

/**
 * 获取验证码
 */
const getCode = async () => {
  const res = await getCodeImg();
  const { data } = res;
  captchaEnabled.value = data.captchaEnabled === undefined ? true : data.captchaEnabled;
  if (captchaEnabled.value) {
    // 刷新验证码时清空输入框
    loginForm.value.code = '';
    codeUrl.value = 'data:image/gif;base64,' + data.img;
    loginForm.value.uuid = data.uuid;
  }
};

const getLoginData = () => {
  const tenantId = localStorage.getItem('tenantId');
  const username = localStorage.getItem('username');
  const password = localStorage.getItem('password');
  const rememberMe = localStorage.getItem('rememberMe');
  loginForm.value = {
    tenantId: tenantId === null ? String(loginForm.value.tenantId) : tenantId,
    username: username === null ? String(loginForm.value.username) : username,
    password: password === null ? String(loginForm.value.password) : String(password),
    rememberMe: rememberMe === null ? false : Boolean(rememberMe)
  } as LoginData;
};

/**
 * 获取租户列表
 */
const initTenantList = async () => {
  tenantEnabled.value = false;
  loginForm.value.tenantId = '000000';
  localStorage.setItem('tenantId', '000000');
  return;
  const { data } = await getTenantList(false);
  tenantEnabled.value = data.tenantEnabled === undefined ? true : data.tenantEnabled;
  if (tenantEnabled.value) {
    tenantList.value = data.voList;
    if (tenantList.value != null && tenantList.value.length !== 0) {
      loginForm.value.tenantId = tenantList.value[0].tenantId;
    }
  }
};

onMounted(() => {
  getCode();
  initTenantList();
  getLoginData();
});
</script>

<style lang="scss" scoped>
.login {
  position: relative;
  display: flex;
  min-height: 100%;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 28px;
  background:
    linear-gradient(180deg, #0e86d9 0 26%, #5bbde7 26% 38%, transparent 38%),
    linear-gradient(180deg, #2f9357 0 52%, #78bd55 52% 70%, #b9632a 70% 100%);
  color: #2c1a10;
  image-rendering: pixelated;
}

.login::before {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 128px;
  background:
    linear-gradient(90deg, #216a37 0 34px, transparent 34px 48px, #2f9357 48px 84px, transparent 84px 100px) 0 40px / 120px 70px repeat-x,
    linear-gradient(90deg, #6d3f1c 0 10px, transparent 10px 42px) 0 8px / 52px 48px repeat-x,
    linear-gradient(180deg, transparent 0 18px, #7a451c 18px 28px, transparent 28px) 0 0 / 100% 48px,
    linear-gradient(180deg, #b9632a, #9d5828);
  border-top: 5px solid #6d3f1c;
}

.login::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 58px;
  height: 118px;
  background:
    linear-gradient(135deg, transparent 0 48%, #1d7447 48% 52%, transparent 52%) 30px 0 / 220px 118px repeat-x,
    linear-gradient(45deg, transparent 0 48%, #29945d 48% 52%, transparent 52%) 120px 12px / 240px 112px repeat-x;
  opacity: 0.86;
}

.login-scene {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.pixel-cloud {
  position: absolute;
  width: 126px;
  height: 42px;
  background:
    linear-gradient(#fff3d1 0 0) 0 18px / 40px 18px no-repeat,
    linear-gradient(#fff3d1 0 0) 28px 6px / 56px 30px no-repeat,
    linear-gradient(#fff3d1 0 0) 76px 16px / 50px 20px no-repeat;
  filter: drop-shadow(4px 4px 0 rgba(45, 96, 122, 0.22));
}

.cloud-a {
  top: 34px;
  left: 18%;
}

.cloud-b {
  top: 42px;
  right: 20%;
  transform: scale(1.14);
}

.pixel-house {
  position: absolute;
  right: 8%;
  bottom: 126px;
  width: 132px;
  height: 104px;
  background:
    linear-gradient(#5c3219 0 0) 42px 52px / 30px 52px no-repeat,
    linear-gradient(#69b7d9 0 0) 88px 50px / 22px 22px no-repeat,
    linear-gradient(#a86530 0 0) 18px 32px / 98px 72px no-repeat,
    linear-gradient(135deg, transparent 0 34%, #c74b26 34% 66%, transparent 66%) 0 0 / 132px 54px no-repeat;
  filter: drop-shadow(6px 6px 0 #5c3219);
}

.pixel-fence {
  position: absolute;
  left: 5%;
  right: 5%;
  bottom: 146px;
  height: 44px;
  background:
    linear-gradient(90deg, #6d3f1c 0 10px, transparent 10px 48px) 0 0 / 58px 44px repeat-x,
    linear-gradient(180deg, transparent 0 12px, #8a4f24 12px 22px, transparent 22px 30px, #8a4f24 30px 40px, transparent 40px);
}

.login-shell {
  position: relative;
  z-index: 1;
  display: grid;
  width: min(980px, 94vw);
  grid-template-columns: minmax(260px, 0.88fr) minmax(340px, 420px);
  gap: 16px;
  align-items: stretch;
  padding: 14px;
  border: 5px solid #6d3f1c;
  border-radius: 10px;
  background: linear-gradient(180deg, #d88a34, #b86d26);
  box-shadow: 0 8px 0 #5c3219, 0 24px 42px rgba(62, 34, 11, 0.35);
}

.login-sidebar,
.login-form {
  border: 4px solid #8a4f24;
  border-radius: 8px;
  background: #ffd995;
  box-shadow: inset 0 0 0 3px #f3b65d, 0 4px 0 #6d3f1c;
}

.login-sidebar {
  position: relative;
  overflow: hidden;
  min-height: 440px;
  padding: 22px;
  background:
    linear-gradient(180deg, rgba(255, 244, 196, 0.42), transparent 42%),
    url('@/assets/images/dzg-farm-banner.svg') center bottom / cover no-repeat,
    #ffd995;
}

.wood-sign {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  padding: 12px 18px;
  border: 4px solid #5c3219;
  border-radius: 8px;
  background: linear-gradient(180deg, #db8b35, #b86d26);
  color: #fff6d7;
  box-shadow: 0 5px 0 #5c3219;
  font-size: clamp(30px, 4vw, 48px);
  font-weight: 900;
  line-height: 1;
  text-shadow: 3px 3px 0 #5c3219;
}

.sign-icon {
  display: inline-grid;
  width: 52px;
  height: 52px;
  place-items: center;
  border: 3px solid #5c3219;
  border-radius: 6px;
  background: #fff0b8;
  color: #5c3219;
  text-shadow: none;
  font-size: 28px;
}

.side-card {
  width: min(320px, 100%);
  margin-top: 26px;
  padding: 16px;
  border: 3px solid #9a5a24;
  border-radius: 8px;
  background: rgba(255, 231, 169, 0.92);
  box-shadow: 0 4px 0 #8a4f24;
}

.side-card h2 {
  margin: 0 0 8px;
  color: #3b1d0c;
  font-size: 24px;
}

.side-card p {
  margin: 0;
  color: #57351e;
  font-size: 16px;
  line-height: 1.6;
}

.pixel-crops {
  position: absolute;
  left: 22px;
  bottom: 18px;
  display: flex;
  gap: 10px;
}

.pixel-crops i {
  width: 26px;
  height: 42px;
  background:
    linear-gradient(#2d8b45 0 0) 6px 0 / 14px 18px no-repeat,
    linear-gradient(#3fae5a 0 0) 0 16px / 26px 18px no-repeat,
    linear-gradient(#6d3f1c 0 0) 11px 28px / 6px 14px no-repeat;
}

.title-box {
  margin-bottom: 20px;
  text-align: center;
}

.eyebrow {
  margin: 0 0 8px;
  color: #7a451c;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.title {
  margin: 0;
  color: #3b1d0c;
  font-size: 32px;
  font-weight: 900;
  line-height: 1.15;
  text-shadow: 2px 2px 0 #f3b65d;
}

.subtitle {
  margin: 8px 0 0;
  color: #6c4628;
  font-size: 15px;
  font-weight: 700;
}

.login-form {
  width: 100%;
  padding: 30px 28px 18px;
  z-index: 1;
}

.login-form .el-input {
  height: 44px;

  input {
    height: 44px;
  }
}

.login-form .input-icon {
  width: 15px;
  height: 42px;
  margin-left: 0;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 44px;
  border: 2px solid #9a5a24;
  border-radius: 7px;
  background: #fff1bd;
  box-shadow: inset 0 2px 0 #f0c66e, 0 3px 0 #8a4f24;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #2d8b45;
  box-shadow: inset 0 2px 0 #f0c66e, 0 0 0 4px rgba(63, 125, 88, 0.25), 0 3px 0 #8a4f24;
}

.login-code {
  width: calc(37% - 10px);
  height: 44px;
  margin-left: 10px;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #9a5a24;
  border-radius: 7px;
  background: #fff1bd;
  box-shadow: 0 3px 0 #8a4f24;
}

.login-code:focus-visible {
  outline: 3px solid rgba(63, 125, 88, 0.35);
  outline-offset: 3px;
}

.login-code-img {
  display: block;
  width: 100%;
  height: 40px;
  object-fit: cover;
}

.remember-check {
  margin: 0 0 20px;
  color: #4a2a16;
  font-weight: 700;
}

.login-submit {
  width: 100%;
  min-height: 48px;
  border: 3px solid #1f5f30;
  border-radius: 7px;
  background: linear-gradient(180deg, #4da85f, #2d8b45);
  color: #fff8d8;
  box-shadow: 0 5px 0 #1f5f30;
  font-size: 18px;
  font-weight: 900;
  text-shadow: 2px 2px 0 #1f5f30;
}

.login-submit:hover,
.login-submit:focus-visible {
  border-color: #1f5f30;
  background: linear-gradient(180deg, #66be72, #32964c);
  transform: translateY(-1px);
}

.el-login-footer {
  position: fixed;
  right: 18px;
  bottom: 12px;
  z-index: 2;
  padding: 6px 12px;
  border: 2px solid #6d3f1c;
  border-radius: 6px;
  background: rgba(255, 217, 149, 0.9);
  color: #3b1d0c;
  font-size: 12px;
  font-weight: 800;
  box-shadow: 0 3px 0 #6d3f1c;
}

:global(html.dark) {
  .login {
    background:
      linear-gradient(180deg, #123f5d 0 28%, #1f4f64 28% 40%, transparent 40%),
      linear-gradient(180deg, #173629 0 56%, #315b34 56% 70%, #5f361b 70% 100%);
  }

  .login-shell {
    background: linear-gradient(180deg, #8a4f24, #5f361b);
    border-color: #3b2416;
    box-shadow: 0 8px 0 #2a1a10, 0 24px 42px rgba(0, 0, 0, 0.42);
  }

  .login-sidebar,
  .login-form {
    background: #2a3b28;
    border-color: #7a4a24;
    box-shadow: inset 0 0 0 3px #5f361b, 0 4px 0 #2a1a10;
  }

  .title,
  .side-card h2 {
    color: #f8e7bb;
    text-shadow: 2px 2px 0 #2a1a10;
  }

  .subtitle,
  .side-card p,
  .remember-check {
    color: #d8c396;
  }

  .eyebrow {
    color: #d6a84b;
  }

  .side-card,
  .el-login-footer {
    background: rgba(49, 35, 22, 0.92);
    color: #f8e7bb;
  }

  .login-form :deep(.el-input__wrapper),
  .login-code {
    border-color: #7a4a24;
    background: #1f2f23;
    box-shadow: inset 0 2px 0 #365331, 0 3px 0 #2a1a10;
  }
}

@media (max-width: 820px) {
  .login {
    padding: 14px;
    align-items: flex-start;
  }

  .login-shell {
    grid-template-columns: 1fr;
    width: min(440px, 96vw);
    margin-top: 16px;
  }

  .login-sidebar {
    min-height: 220px;
  }

  .side-card {
    display: none;
  }

  .wood-sign {
    font-size: 32px;
  }

  .pixel-house,
  .pixel-fence {
    display: none;
  }
}
</style>
