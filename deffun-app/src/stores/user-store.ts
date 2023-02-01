import { defineStore } from 'pinia';
import { api } from 'boot/axios';
import { UserData } from './UserData';

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null as UserData | null,
  }),
  getters: {
    getCurrentUser: (state) => state.currentUser,
  },
  actions: {
    async fetchUser() {
      this.currentUser = await api
        .get<UserData>('/users/profile')
        .then((response) => {
          return response.data;
        })
        .catch((err) => {
          console.log(err);
          return null;
        });
    },
  },
});
