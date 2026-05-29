export type UserRole = "ADMIN" | "HERO" | "VILLAIN" | "OPERATOR";

export type AuthUser = {
  id: string;
  nome: string;
  email: string;
  role: UserRole;
  heroiId: string | null;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: string;
  user: AuthUser;
};
