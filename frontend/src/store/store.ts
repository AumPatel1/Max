import { configureStore } from "@reduxjs/toolkit";
import footerReducer from "./footerSlice";
import categoryReducer from "./categorySlice";
import searchReducer from "./searchSlice";
import homeReducer from "./homeSlice";

export const store = configureStore({
  reducer: {
    footer: footerReducer,
    category: categoryReducer,
    search: searchReducer,
    home: homeReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
