import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ROUTES } from "../constants/routes";

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: "coach" | "member";
  redirectTo?: string;
}

const ProtectedRouteContent: React.FC<ProtectedRouteProps> = ({
  children,
  requiredRole,
  redirectTo = ROUTES.LOGIN,
}) => {
  const { isAuthenticated, user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to={redirectTo} state={{ from: location }} replace />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to={ROUTES.HOME} replace />;
  }

  return <>{children}</>;
};

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredRole,
  redirectTo,
}) => {
  // Development shortcut: skip auth until screens are integrated.
  const BYPASS_AUTH_FOR_SCREEN_BUILD = true;
  if (BYPASS_AUTH_FOR_SCREEN_BUILD) {
    return <>{children}</>;
  }

  return (
    <ProtectedRouteContent requiredRole={requiredRole} redirectTo={redirectTo}>
      {children}
    </ProtectedRouteContent>
  );
};
