import { useMutation } from "@tanstack/react-query";
import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

import authService, { type LoginReq } from "@/api/services/authService";

import type { UserInfo, UserToken } from "#/entity";
import { StorageEnum } from "#/enum";

type UserStore = {
	userInfo: Partial<UserInfo>;
	userToken: UserToken;

	actions: {
		setUserInfo: (userInfo: UserInfo) => void;
		setUserToken: (token: UserToken) => void;
		clearUserInfoAndToken: () => void;
	};
};

const useUserStore = create<UserStore>()(
	persist(
		(set) => ({
			userInfo: {},
			userToken: {},
			actions: {
				setUserInfo: (userInfo) => {
					set({ userInfo });
				},
				setUserToken: (userToken) => {
					set({ userToken });
				},
				clearUserInfoAndToken() {
					set({ userInfo: {}, userToken: {} });
				},
			},
		}),
		{
			name: "userStore",
			version: 2,
			storage: createJSONStorage(() => localStorage),
			partialize: (state) => ({
				[StorageEnum.UserInfo]: state.userInfo,
				[StorageEnum.UserToken]: state.userToken,
			}),
			migrate: (persistedState, version) => {
				if (version < 2) {
					return { userInfo: {}, userToken: {} } as Partial<UserStore>;
				}
				return persistedState as Partial<UserStore>;
			},
		},
	),
);

export const useUserInfo = () => useUserStore((state) => state.userInfo);
export const useUserToken = () => useUserStore((state) => state.userToken);
export const useUserRole = () => useUserStore((state) => state.userInfo.role);
export const useUserActions = () => useUserStore((state) => state.actions);

export const useSignIn = () => {
	const { setUserToken, setUserInfo, clearUserInfoAndToken } = useUserActions();

	const signInMutation = useMutation({
		mutationFn: authService.login,
	});

	const signIn = async (data: LoginReq) => {
		const loginRes = await signInMutation.mutateAsync(data);
		setUserToken({ accessToken: loginRes.accessToken, expiresIn: loginRes.expiresIn });
		try {
			const me = await authService.me();
			setUserInfo({ username: me.username, role: me.role });
			return me;
		} catch (err) {
			clearUserInfoAndToken();
			throw err;
		}
	};

	return signIn;
};

export default useUserStore;
