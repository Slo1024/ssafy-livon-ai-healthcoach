import { useState, useCallback } from 'react';

interface ModalState {
  isOpen: boolean;
  data?: any;
}

export const useModal = () => {
  const [modalState, setModalState] = useState<ModalState>({
    isOpen: false,
    data: null,
  });

  const openModal = useCallback((data?: any) => {
    setModalState({
      isOpen: true,
      data,
    });
  }, []);

  const closeModal = useCallback(() => {
    setModalState({
      isOpen: false,
      data: null,
    });
  }, []);

  const toggleModal = useCallback(() => {
    setModalState(prev => ({
      isOpen: !prev.isOpen,
      data: prev.isOpen ? null : prev.data,
    }));
  }, []);

  return {
    isOpen: modalState.isOpen,
    data: modalState.data,
    openModal,
    closeModal,
    toggleModal,
  };
};
