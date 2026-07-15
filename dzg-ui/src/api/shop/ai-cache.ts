import { getShopAiBusinessAnalysis } from './ai';

type BusinessAnalysisResponse = Awaited<ReturnType<typeof getShopAiBusinessAnalysis>>;

const CACHE_TTL = 3 * 60 * 1000;

let cachedResponse: BusinessAnalysisResponse | undefined;
let cachedAt = 0;
let pendingRequest: Promise<BusinessAnalysisResponse> | undefined;

const hasFreshCache = () => Boolean(cachedResponse && Date.now() - cachedAt < CACHE_TTL);

export const getCachedShopAiBusinessAnalysis = (force = false) => {
  if (!force && hasFreshCache()) {
    return Promise.resolve(cachedResponse as BusinessAnalysisResponse);
  }

  if (pendingRequest) {
    return pendingRequest;
  }

  pendingRequest = getShopAiBusinessAnalysis()
    .then((res) => {
      cachedResponse = res;
      cachedAt = Date.now();
      return res;
    })
    .finally(() => {
      pendingRequest = undefined;
    });

  return pendingRequest;
};

export const preloadShopAiBusinessAnalysis = () => {
  return getCachedShopAiBusinessAnalysis(false).catch(() => undefined);
};

export const clearShopAiBusinessAnalysisCache = () => {
  cachedResponse = undefined;
  cachedAt = 0;
};
